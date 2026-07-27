package com.thanh0x.coursedeal.service;

import com.thanh0x.coursedeal.crawler_runner.UdemyCouponCourseExtractor;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import com.thanh0x.coursedeal.model.coupon.CouponCourseHistory;
import com.thanh0x.coursedeal.repository.CouponCourseHistoryRepository;
import com.thanh0x.coursedeal.repository.CouponCourseRepository;
import com.thanh0x.coursedeal.repository.ExpiredCouponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling asynchronous scraping of Udemy coupons.
 * This prevents slow external API calls from blocking the main request threads.
 */
@Service
public class UdemyScraperService {
    private static final Logger log = LoggerFactory.getLogger(UdemyScraperService.class);

    private final CouponCourseRepository couponCourseRepository;
    private final ExpiredCouponRepository expiredCouponRepository;
    private final CouponCourseHistoryRepository couponCourseHistoryRepository;
    private final NotificationService notificationService;

    public UdemyScraperService(CouponCourseRepository couponCourseRepository,
                               ExpiredCouponRepository expiredCouponRepository,
                               CouponCourseHistoryRepository couponCourseHistoryRepository,
                               NotificationService notificationService) {
        this.couponCourseRepository = couponCourseRepository;
        this.expiredCouponRepository = expiredCouponRepository;
        this.couponCourseHistoryRepository = couponCourseHistoryRepository;
        this.notificationService = notificationService;
    }

    /**
     * Asynchronously validates and saves a new coupon URL.
     * 
     * @param couponUrl  the Udemy coupon URL to validate
     * @param remoteAddr the address of the requestor for logging
     */
    @Async("scraperExecutor")
    @Transactional
    public void validateAndSaveCouponAsync(String couponUrl, String remoteAddr) {
        log.info("Starting async validation for coupon: {} from {}", couponUrl, remoteAddr);
        
        try {
            UdemyCouponCourseExtractor extractor = new UdemyCouponCourseExtractor(couponUrl);
            CouponCourseData couponData = extractor.getFullCouponCodeData();
            
            if (couponData == null) {
                log.warn("Failed to extract valid data for URL: {}", couponUrl);
                return;
            }

            boolean existedBefore = couponCourseRepository.findByCouponUrl(couponUrl) != null
                    || expiredCouponRepository.findByCouponUrl(couponUrl) != null;
            couponData.setNew(!existedBefore);

            CouponCourseData saved = couponCourseRepository.save(couponData);
            
            couponCourseHistoryRepository.save(CouponCourseHistory.builder()
                .courseId(saved.getCourseId())
                .title(saved.getTitle())
                .couponUrl(saved.getCouponUrl())
                .status(existedBefore ? "reactivated" : "new")
                .build());
                
            log.info("Successfully validated and saved: {} (ID: {})", saved.getTitle(), saved.getCourseId());

            // Notify users about the new deal
            notificationService.broadcastNewCoupon(saved.getTitle(), saved.getCategory());
            
        } catch (Exception e) {
            log.error("Error in async scraping for {}: {}", couponUrl, e.getMessage(), e);
        }
    }
}
