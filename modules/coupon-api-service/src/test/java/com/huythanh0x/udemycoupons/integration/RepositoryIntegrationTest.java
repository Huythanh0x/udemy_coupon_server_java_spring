package com.huythanh0x.udemycoupons.integration;

import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.model.coupon.ExpiredCourseData;
import com.huythanh0x.udemycoupons.model.coupon.ScrapedUrlMapping;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import com.huythanh0x.udemycoupons.repository.ExpiredCouponRepository;
import com.huythanh0x.udemycoupons.repository.ScrapedUrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for repositories using in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private CouponCourseRepository couponCourseRepository;

    @Autowired
    private ExpiredCouponRepository expiredCouponRepository;

    @Autowired
    private ScrapedUrlMappingRepository scrapedUrlMappingRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        couponCourseRepository.deleteAll();
        expiredCouponRepository.deleteAll();
        scrapedUrlMappingRepository.deleteAll();
    }

    @Test
    void testCouponCourseRepository_FindAllCouponUrls() {
        // Arrange
        CouponCourseData coupon1 = CouponCourseData.builder()
                .courseId(1)
                .couponUrl("https://www.udemy.com/course/test1/?couponCode=ABC")
                .expiredDate(Instant.now().plusSeconds(3600))
                .usesRemaining(100)
                .build();
        CouponCourseData coupon2 = CouponCourseData.builder()
                .courseId(2)
                .couponUrl("https://www.udemy.com/course/test2/?couponCode=XYZ")
                .expiredDate(Instant.now().plusSeconds(3600))
                .usesRemaining(50)
                .build();
        couponCourseRepository.save(coupon1);
        couponCourseRepository.save(coupon2);

        // Act
        Set<String> urls = couponCourseRepository.findAllCouponUrls();

        // Assert
        assertEquals(2, urls.size());
        assertTrue(urls.contains(coupon1.getCouponUrl()));
        assertTrue(urls.contains(coupon2.getCouponUrl()));
    }

    @Test
    void testCouponCourseRepository_FindCouponUrlsNeedingRefresh() {
        // Arrange
        Instant soonExpiring = Instant.now().plusSeconds(3600); // 1 hour from now
        Instant farExpiring = Instant.now().plusSeconds(86400); // 24 hours from now

        CouponCourseData expiringSoon = CouponCourseData.builder()
                .courseId(1)
                .couponUrl("https://www.udemy.com/course/expiring/?couponCode=ABC")
                .expiredDate(soonExpiring)
                .usesRemaining(100)
                .build();

        CouponCourseData lowUses = CouponCourseData.builder()
                .courseId(2)
                .couponUrl("https://www.udemy.com/course/lowuses/?couponCode=XYZ")
                .expiredDate(farExpiring)
                .usesRemaining(30) // Less than 50
                .build();

        CouponCourseData normal = CouponCourseData.builder()
                .courseId(3)
                .couponUrl("https://www.udemy.com/course/normal/?couponCode=DEF")
                .expiredDate(farExpiring)
                .usesRemaining(100)
                .build();

        couponCourseRepository.save(expiringSoon);
        couponCourseRepository.save(lowUses);
        couponCourseRepository.save(normal);

        // Act - Find coupons expiring within 2 hours or with uses < 50
        Instant threshold = Instant.now().plusSeconds(7200); // 2 hours
        Set<String> urlsNeedingRefresh = couponCourseRepository.findCouponUrlsNeedingRefresh(threshold, 50);

        // Assert
        assertTrue(urlsNeedingRefresh.size() >= 2);
        assertTrue(urlsNeedingRefresh.contains(expiringSoon.getCouponUrl()));
        assertTrue(urlsNeedingRefresh.contains(lowUses.getCouponUrl()));
        assertFalse(urlsNeedingRefresh.contains(normal.getCouponUrl()));
    }

    @Test
    void testExpiredCouponRepository_FindCouponUrlsExpiredInLastDays() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime fiveDaysAgo = now.minusDays(5);

        // Create entities - @CreationTimestamp will set createdAt to current time on first save
        ExpiredCourseData recent = new ExpiredCourseData("https://www.udemy.com/course/recent/?couponCode=ABC");
        expiredCouponRepository.saveAndFlush(recent);
        
        ExpiredCourseData old = new ExpiredCourseData("https://www.udemy.com/course/old/?couponCode=XYZ");
        expiredCouponRepository.saveAndFlush(old);
        
        // Update timestamps using native SQL since @CreationTimestamp prevents manual setting via setter
        entityManager.createNativeQuery(
            "UPDATE expired_course_data SET created_at = :timestamp WHERE coupon_url = :url"
        )
        .setParameter("timestamp", twoDaysAgo)
        .setParameter("url", recent.getCouponUrl())
        .executeUpdate();
        
        entityManager.createNativeQuery(
            "UPDATE expired_course_data SET created_at = :timestamp WHERE coupon_url = :url"
        )
        .setParameter("timestamp", fiveDaysAgo)
        .setParameter("url", old.getCouponUrl())
        .executeUpdate();
        
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure we read from DB

        // Act - Find expired in last 3 days
        LocalDateTime cutoff = now.minusDays(3);
        Set<String> recentExpired = expiredCouponRepository.findCouponUrlsExpiredInLastDays(cutoff);

        // Assert
        assertEquals(1, recentExpired.size(), "Should find exactly 1 expired coupon from last 3 days");
        assertTrue(recentExpired.contains(recent.getCouponUrl()), "Should contain the recent expired coupon");
        assertFalse(recentExpired.contains(old.getCouponUrl()), "Should not contain the old expired coupon");
    }

    @Test
    void testScrapedUrlMappingRepository_SaveAndFind() {
        // Arrange
        ScrapedUrlMapping mapping = ScrapedUrlMapping.builder()
                .scrapedUrl("https://example.com/course/123")
                .couponUrl("https://www.udemy.com/course/test/?couponCode=ABC")
                .crawlerSource("enext")
                .build();

        // Act
        scrapedUrlMappingRepository.save(mapping);
        ScrapedUrlMapping found = scrapedUrlMappingRepository.findByScrapedUrl("https://example.com/course/123");

        // Assert
        assertNotNull(found);
        assertEquals(mapping.getCouponUrl(), found.getCouponUrl());
        assertEquals(mapping.getCrawlerSource(), found.getCrawlerSource());
    }
}

