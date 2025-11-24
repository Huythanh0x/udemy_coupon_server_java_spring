package com.huythanh0x.udemycoupons.crawler_runner;

import com.huythanh0x.udemycoupons.crawler_runner.crawler.EnextCrawler;
import com.huythanh0x.udemycoupons.crawler_runner.crawler.RealDiscountCrawler;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseHistory;
import com.huythanh0x.udemycoupons.model.coupon.ExpiredCourseData;
import com.huythanh0x.udemycoupons.repository.CouponCourseHistoryRepository;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import com.huythanh0x.udemycoupons.repository.ExpiredCouponRepository;
import com.huythanh0x.udemycoupons.utils.LastFetchTimeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * CrawlerRunner class is responsible for running the web crawlers to fetch and save coupon data.
 */
@Component
@ComponentScan("com.huythanh0x.udemycoupons.repository")
public class CrawlerRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CrawlerRunner.class);
    CouponCourseRepository couponCourseRepository;
    ExpiredCouponRepository expiredCouponRepository;
    CouponCourseHistoryRepository couponCourseHistoryRepository;
    EnextCrawler enextCrawler;
    RealDiscountCrawler realDiscountCrawler;
    Integer intervalTime;
    @Value("${custom.number-of-request-thread}")
    Integer numberOfThread;
    @Value("${custom.skip-recently-checked-expired-hours:12}")
    Integer skipRecentlyCheckedExpiredHours;
    @Value("${custom.refresh-expiring-hours:2}")
    Integer refreshExpiringHours;
    @Value("${custom.refresh-min-uses-remaining:50}")
    Integer refreshMinUsesRemaining;
    @Value("${custom.refresh-old-hours:1}")
    Integer refreshOldHours;
    @Value("${custom.enable-smart-refresh:true}")
    Boolean enableSmartRefresh;
    @Value("${custom.batch-processing-size:100}")
    Integer batchProcessingSize;
    private static final String HISTORY_STATUS_NEW = "new";
    private static final String HISTORY_STATUS_REACTIVATED = "reactivated";
    private static final String HISTORY_STATUS_REFRESHED = "refreshed";
    private static final String HISTORY_STATUS_EXPIRED = "expired";

    /**
     * Executes the method to start the crawler when the application runs.
     *
     * @param args The arguments passed to the application upon execution.
     */
    @Override
    public void run(ApplicationArguments args) {
        startCrawler();
    }

    public CrawlerRunner(CouponCourseRepository couponCourseRepository,
                         ExpiredCouponRepository expiredCouponRepository,
                         CouponCourseHistoryRepository couponCourseHistoryRepository,
                         EnextCrawler enextCrawler,
                         RealDiscountCrawler realDiscountCrawler,
                         @Value("${custom.interval-time}") Integer intervalTime) {
        this.couponCourseRepository = couponCourseRepository;
        this.expiredCouponRepository = expiredCouponRepository;
        this.couponCourseHistoryRepository = couponCourseHistoryRepository;
        this.enextCrawler = enextCrawler;
        this.realDiscountCrawler = realDiscountCrawler;
        this.intervalTime = intervalTime;
    }

    /**
     * Starts the crawler process that continuously fetches coupon URLs, filters them,
     * saves data, and delays until the next round.
     *
     * Uses a single thread executor to execute the crawler in a separate thread.
     *
     * @throws InterruptedException if the thread is interrupted during execution.
     */
    public void startCrawler() {
        AtomicLong startTime = new AtomicLong(LastFetchTimeManager.loadLasFetchedTimeInMilliSecond());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                delayUntilTheNextRound(startTime.get());
                while (true) {
                    startTime.set(System.currentTimeMillis());
                    
                    List<String> scrapedCouponUrls = new ArrayList<>();
                    scrapedCouponUrls.addAll(enextCrawler.getAllCouponUrls());
                    scrapedCouponUrls.addAll(realDiscountCrawler.getAllCouponUrls());
                    
                    Set<String> allCouponsNeedingRefresh = new HashSet<>();
                    Instant expirationThreshold = Instant.now().plusSeconds(refreshExpiringHours * 3600L);
                    LocalDateTime updatedBefore = LocalDateTime.now().minusHours(refreshOldHours);
                    
                    allCouponsNeedingRefresh = couponCourseRepository.findCouponUrlsNeedingRefresh(
                        expirationThreshold, 
                        refreshMinUsesRemaining,
                        updatedBefore
                    );
                    log.info("Found {} coupons needing refresh (expiring within {} hours, uses remaining < {}, not updated in last {} hours)",
                            allCouponsNeedingRefresh.size(), refreshExpiringHours, refreshMinUsesRemaining, refreshOldHours);
                    
                    Set<String> allUrlsToProcess = new HashSet<>(scrapedCouponUrls);
                    allUrlsToProcess.addAll(allCouponsNeedingRefresh);
                    
                    Set<String> newlyScrapedUrls = new HashSet<>(scrapedCouponUrls);
                    List<String> filterCouponUrls = filterValidCouponUrls(
                        new ArrayList<>(allUrlsToProcess), 
                        allCouponsNeedingRefresh,
                        newlyScrapedUrls
                    );
                    
                    saveAllCouponData(filterCouponUrls, numberOfThread);
                    delayUntilTheNextRound(startTime.get());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Crawler thread interrupted", e);
            }
        });
    }

    /**
     * Delays the execution until the next round by calculating the time elapsed
     * since the start and the remaining time until the next interval.
     *
     * @param startTime the start time of the current round
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    private void delayUntilTheNextRound(long startTime) throws InterruptedException {
        long runTime = System.currentTimeMillis() - startTime;
        long delayTime = Math.max(intervalTime - runTime, 0);
        log.info("Waiting {} ms until the next run", delayTime);
        Thread.sleep(delayTime);
    }

    /**
     * Saves all coupon data by processing URLs in batches sequentially.
     * Each batch processes URLs concurrently (using numberOfThread), but batches run one after another
     * to avoid hitting rate limits (429 errors) from Udemy API.
     * Each batch is saved immediately to the database for faster user visibility.
     *
     * @param allCouponUrls List of URLs containing coupon data to be processed
     * @param numberOfThread Number of threads to execute concurrently within each batch
     */
    private void saveAllCouponData(List<String> allCouponUrls, int numberOfThread) {
        log.info("Processing {} URLs in batches of {}", allCouponUrls.size(), batchProcessingSize);
        
        Set<String> allExpiredCouponUrls = new HashSet<>();
        int totalProcessed = 0;
        
        while (totalProcessed < allCouponUrls.size()) {
            List<String> batch = allCouponUrls.stream()
                    .skip(totalProcessed)
                    .limit(batchProcessingSize)
                    .collect(Collectors.toList());
            
            log.info("Processing batch {}-{} of {}", totalProcessed + 1,
                    Math.min(totalProcessed + batchProcessingSize, allCouponUrls.size()),
                    allCouponUrls.size());
            
            BatchResult batchResult = processBatch(batch, numberOfThread);
            
            if (!batchResult.validCoupons.isEmpty()) {
                couponCourseRepository.saveAll(batchResult.validCoupons);
                
                Set<String> validUrls = batchResult.validCoupons.stream()
                    .map(CouponCourseData::getCouponUrl)
                    .filter(url -> url != null)
                    .collect(Collectors.toSet());
                
                for (String validUrl : validUrls) {
                    ExpiredCourseData expiredEntry = expiredCouponRepository.findByCouponUrl(validUrl);
                    if (expiredEntry != null) {
                        expiredCouponRepository.delete(expiredEntry);
                    }
                }
                
                log.info("Saved {} valid coupons to database", batchResult.validCoupons.size());
            }

            if (!batchResult.expiredCoupons.isEmpty()) {
                Set<String> existingExpiredUrls = new HashSet<>();
                List<ExpiredCourseData> expiredToUpdate = new ArrayList<>();
                List<ExpiredCourseData> expiredToCreate = new ArrayList<>();
                
                for (ExpiredCouponInfo expiredInfo : batchResult.expiredCoupons) {
                    ExpiredCourseData existingExpired = expiredCouponRepository.findByCouponUrl(expiredInfo.couponUrl);
                    if (existingExpired != null) {
                        existingExpiredUrls.add(expiredInfo.couponUrl);
                        boolean needsUpdate = false;
                        if (existingExpired.getCourseId() == null && expiredInfo.courseId != null) {
                            existingExpired.setCourseId(expiredInfo.courseId);
                            needsUpdate = true;
                        }
                        if (existingExpired.getTitle() == null && expiredInfo.title != null) {
                            existingExpired.setTitle(expiredInfo.title);
                            needsUpdate = true;
                        }
                        if (needsUpdate) {
                            expiredToUpdate.add(existingExpired);
                        }
                    } else {
                        expiredToCreate.add(new ExpiredCourseData(
                            expiredInfo.couponUrl, 
                            expiredInfo.courseId, 
                            expiredInfo.title
                        ));
                    }
                }
                
                if (!existingExpiredUrls.isEmpty()) {
                    expiredCouponRepository.updateUpdatedAtForUrls(existingExpiredUrls);
                }
                
                if (!expiredToUpdate.isEmpty()) {
                    expiredCouponRepository.saveAll(expiredToUpdate);
                }
                
                if (!expiredToCreate.isEmpty()) {
                    expiredCouponRepository.saveAll(expiredToCreate);
                }
                
                Set<String> expiredUrls = batchResult.expiredCoupons.stream()
                    .map(info -> info.couponUrl)
                    .collect(Collectors.toSet());
                allExpiredCouponUrls.addAll(expiredUrls);
                
                log.info("Saved/Updated {} expired coupons ({} new, {} updated)",
                        batchResult.expiredCoupons.size(), expiredToCreate.size(), existingExpiredUrls.size());
                LastFetchTimeManager.updateLastBulkRefreshCoupon();
            }

            if (!batchResult.failedToValidateCouponUrls.isEmpty()) {
                log.warn("{} URLs failed to validate", batchResult.failedToValidateCouponUrls.size());
            }

            if (!batchResult.historyEntries.isEmpty()) {
                couponCourseHistoryRepository.saveAll(batchResult.historyEntries);
            }
            
            totalProcessed += batch.size();
            log.info("Progress: {}/{} URLs processed", totalProcessed, allCouponUrls.size());
        }
        
        if (!allExpiredCouponUrls.isEmpty()) {
            couponCourseRepository.deleteAllCouponsByUrl(allExpiredCouponUrls);
            log.info("Cleaned up {} expired coupons from main table", allExpiredCouponUrls.size());
        }
        
        log.info("All batches finished. Total processed: {}", totalProcessed);
        LastFetchTimeManager.updateLastBulkRefreshCoupon();
    }

    /**
     * Processes a batch of coupon URLs concurrently and returns the results.
     *
     * @param batch List of coupon URLs to process
     * @param numberOfThread Number of threads to execute concurrently
     * @return BatchResult containing valid coupons, expired URLs, and failed validations
     */
    private BatchResult processBatch(List<String> batch, int numberOfThread) {
        Set<CouponCourseData> validCoupons = Collections.synchronizedSet(new HashSet<>());
        Set<String> failedToValidateCouponUrls = Collections.synchronizedSet(new HashSet<>());
        Set<ExpiredCouponInfo> expiredCoupons = Collections.synchronizedSet(new HashSet<>());
        List<CouponCourseHistory> historyEntries = Collections.synchronizedList(new ArrayList<>());
        
        Map<String, Integer> courseIdCache = new HashMap<>();
        Map<String, CourseState> courseStateCache = new HashMap<>();
        for (String couponUrl : batch) {
            Integer courseId = couponCourseRepository.findCourseIdByCouponUrl(couponUrl);
            if (courseId != null) {
                courseIdCache.put(couponUrl, courseId);
                courseStateCache.put(couponUrl, CourseState.ACTIVE);
                continue;
            }
            courseId = expiredCouponRepository.findCourseIdByCouponUrl(couponUrl);
            if (courseId != null) {
                courseIdCache.put(couponUrl, courseId);
                courseStateCache.put(couponUrl, CourseState.EXPIRED);
            } else {
                courseStateCache.put(couponUrl, CourseState.NEW);
            }
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
        for (String couponUrl : batch) {
            executor.submit(() -> {
                try {
                    Integer cachedCourseId = courseIdCache.get(couponUrl);
                    UdemyCouponCourseExtractor extractor = (cachedCourseId != null)
                        ? new UdemyCouponCourseExtractor(couponUrl, cachedCourseId)
                        : new UdemyCouponCourseExtractor(couponUrl);
                    
                    CouponCourseData couponCodeData = extractor.getFullCouponCodeData();
                    if (couponCodeData != null) {
                        CourseState state = courseStateCache.getOrDefault(couponUrl, CourseState.NEW);
                        boolean isFirstTime = state == CourseState.NEW;
                        couponCodeData.setNew(isFirstTime);
                        validCoupons.add(couponCodeData);
                        historyEntries.add(CouponCourseHistory.builder()
                            .courseId(couponCodeData.getCourseId())
                            .title(couponCodeData.getTitle())
                            .couponUrl(couponUrl)
                            .status(switch (state) {
                                case NEW -> HISTORY_STATUS_NEW;
                                case EXPIRED -> HISTORY_STATUS_REACTIVATED;
                                case ACTIVE -> HISTORY_STATUS_REFRESHED;
                            })
                            .build());
                        courseIdCache.put(couponUrl, couponCodeData.getCourseId());
                        courseStateCache.put(couponUrl, CourseState.ACTIVE);
                        log.debug("Validated coupon {}", couponCodeData.getTitle());
                    } else {
                        Integer courseId = resolveCourseIdForExpired(couponUrl, extractor, cachedCourseId);
                        String title = null;
                        CouponCourseData existing = couponCourseRepository.findByCouponUrl(couponUrl);
                        if (existing != null) {
                            title = existing.getTitle();
                            if (courseId == null) {
                                courseId = existing.getCourseId();
                            }
                        }
                        if (title == null) {
                            ExpiredCourseData expiredData = expiredCouponRepository.findByCouponUrl(couponUrl);
                            if (expiredData != null) {
                                title = expiredData.getTitle();
                                if (courseId == null) {
                                    courseId = expiredData.getCourseId();
                                }
                            }
                        }
                        
                        expiredCoupons.add(new ExpiredCouponInfo(couponUrl, courseId, title));
                        historyEntries.add(CouponCourseHistory.builder()
                            .courseId(courseId)
                            .title(title)
                            .couponUrl(couponUrl)
                            .status(HISTORY_STATUS_EXPIRED)
                            .build());
                    }
                } catch (Exception e) {
                    failedToValidateCouponUrls.add(couponUrl + " " + e.getMessage());
                    log.warn("Failed to validate coupon {}: {}", couponUrl, e.getMessage(), e);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait until all threads are finished
        }
        
        return new BatchResult(validCoupons, failedToValidateCouponUrls, expiredCoupons, historyEntries);
    }

    /**
     * Helper class to hold batch processing results.
     */
    private static class BatchResult {
        final Set<CouponCourseData> validCoupons;
        final Set<String> failedToValidateCouponUrls;
        final Set<ExpiredCouponInfo> expiredCoupons;
        final List<CouponCourseHistory> historyEntries;

        BatchResult(Set<CouponCourseData> validCoupons,
                    Set<String> failedToValidateCouponUrls,
                    Set<ExpiredCouponInfo> expiredCoupons,
                    List<CouponCourseHistory> historyEntries) {
            this.validCoupons = validCoupons;
            this.failedToValidateCouponUrls = failedToValidateCouponUrls;
            this.expiredCoupons = expiredCoupons;
            this.historyEntries = historyEntries;
        }
    }

    private Integer resolveCourseIdForExpired(String couponUrl, UdemyCouponCourseExtractor extractor, Integer cachedCourseId) {
        int extractedCourseId = extractor.getCourseId();
        if (extractedCourseId > 0) {
            return extractedCourseId;
        }
        if (cachedCourseId != null) {
            return cachedCourseId;
        }
        Integer courseId = couponCourseRepository.findCourseIdByCouponUrl(couponUrl);
        if (courseId != null) {
            return courseId;
        }
        return expiredCouponRepository.findCourseIdByCouponUrl(couponUrl);
    }

    private enum CourseState {
        NEW,
        ACTIVE,
        EXPIRED
    }

    /**
     * Helper class to hold expired coupon information.
     */
    private static class ExpiredCouponInfo {
        final String couponUrl;
        final Integer courseId;
        final String title;

        ExpiredCouponInfo(String couponUrl, Integer courseId, String title) {
            this.couponUrl = couponUrl;
            this.courseId = courseId;
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExpiredCouponInfo that)) return false;
            return Objects.equals(couponUrl, that.couponUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(couponUrl);
        }
    }

    /**
     * Filters coupon URLs using smart pre-validation
     * - For newly scraped URLs: Skip only if recently checked as expired (within X hours)
     *   Otherwise validate even if expired before (might have new coupons)
     * - For refresh URLs: Skip if already in DB (and still valid), or if expired
     * - Allows coupons needing refresh to pass through even if they exist in DB
     * 
     * This reduces API calls while ensuring we don't miss reactivated coupons.
     *
     * @param newCouponUrls the list of new coupon URLs (scraped + refresh URLs)
     * @param couponsNeedingRefresh set of coupon URLs that need to be refreshed (should not be filtered out)
     * @param newlyScrapedUrls set of URLs that were newly scraped from crawlers
     * @return a list of coupon URLs that need to be fetched and validated
     */
    private List<String> filterValidCouponUrls(List<String> newCouponUrls, Set<String> couponsNeedingRefresh, Set<String> newlyScrapedUrls) {
        if (newCouponUrls == null || newCouponUrls.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> existingCouponUrls = couponCourseRepository.findAllCouponUrls();
        
        LocalDateTime recentlyCheckedThreshold = LocalDateTime.now().minusHours(skipRecentlyCheckedExpiredHours);
        Set<String> recentlyCheckedExpiredUrls = expiredCouponRepository.findRecentlyCheckedExpiredUrls(recentlyCheckedThreshold);
        
        return newCouponUrls.stream()
                .filter(couponUrl -> couponUrl != null && !couponUrl.trim().isEmpty())
                .filter(couponUrl -> {
                    if (couponsNeedingRefresh != null && couponsNeedingRefresh.contains(couponUrl)) {
                        return true;
                    }
                    
                    if (newlyScrapedUrls != null && newlyScrapedUrls.contains(couponUrl)) {
                        if (recentlyCheckedExpiredUrls.contains(couponUrl)) {
                            return false;
                        }
                        return true;
                    }
                    
                    if (existingCouponUrls.contains(couponUrl)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
