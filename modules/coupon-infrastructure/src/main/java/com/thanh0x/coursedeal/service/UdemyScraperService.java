package com.thanh0x.coursedeal.service;

import com.thanh0x.coursedeal.crawler_runner.UdemyCouponCourseExtractor;
import com.thanh0x.coursedeal.model.audit.ScrapingTaskLog;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import com.thanh0x.coursedeal.model.coupon.CouponCourseHistory;
import com.thanh0x.coursedeal.repository.CouponCourseHistoryRepository;
import com.thanh0x.coursedeal.repository.CouponCourseRepository;
import com.thanh0x.coursedeal.repository.ExpiredCouponRepository;
import com.thanh0x.coursedeal.repository.audit.ScrapingTaskLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling asynchronous scraping of Udemy coupons.
 */
@Service
public class UdemyScraperService {
    private static final Logger log = LoggerFactory.getLogger(UdemyScraperService.class);

    private final CouponCourseRepository couponCourseRepository;
    private final ExpiredCouponRepository expiredCouponRepository;
    private final CouponCourseHistoryRepository couponCourseHistoryRepository;
    private final NotificationService notificationService;
    private final ScrapingTaskLogRepository scrapingTaskLogRepository;

    public UdemyScraperService(CouponCourseRepository couponCourseRepository,
                               ExpiredCouponRepository expiredCouponRepository,
                               CouponCourseHistoryRepository couponCourseHistoryRepository,
                               NotificationService notificationService,
                               ScrapingTaskLogRepository scrapingTaskLogRepository) {
        this.couponCourseRepository = couponCourseRepository;
        this.expiredCouponRepository = expiredCouponRepository;
        this.couponCourseHistoryRepository = couponCourseHistoryRepository;
        this.notificationService = notificationService;
        this.scrapingTaskLogRepository = scrapingTaskLogRepository;
    }

    /**
     * Asynchronously validates and saves a new coupon URL.
     */
    @Async("scraperExecutor")
    @Transactional
    public void validateAndSaveCouponAsync(String couponUrl, String remoteAddr) {
        log.info("Starting async validation for coupon: {} from {}", couponUrl, remoteAddr);
        
        ScrapingTaskLog auditLog = scrapingTaskLogRepository.save(ScrapingTaskLog.builder()
                .url(couponUrl)
                .remoteAddr(remoteAddr)
                .status("PENDING")
                .build());
        
        try {
            UdemyCouponCourseExtractor extractor = new UdemyCouponCourseExtractor(couponUrl);
            CouponCourseData couponData = extractor.getFullCouponCodeData();
            
            if (couponData == null) {
                log.warn("Failed to extract valid data for URL: {}", couponUrl);
                updateAuditLog(auditLog, "FAILED", "Extractor returned null data");
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

            updateAuditLog(auditLog, "SUCCESS", null, saved.getCourseId());

            // Notify interested users about the new deal
            notificationService.notifyInterestedUsers(saved);
            
        } catch (Exception e) {
            log.error("Error in async scraping for {}: {}", couponUrl, e.getMessage(), e);
            updateAuditLog(auditLog, "FAILED", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void updateAuditLog(ScrapingTaskLog log, String status, String error) {
        updateAuditLog(log, status, error, null);
    }

    private void updateAuditLog(ScrapingTaskLog log, String status, String error, Integer courseId) {
        log.setStatus(status);
        log.setErrorMessage(error);
        log.setCourseId(courseId);
        scrapingTaskLogRepository.save(log);
    }
}
