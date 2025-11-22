package com.huythanh0x.udemycoupons.crawler_runner;

import com.huythanh0x.udemycoupons.crawler_runner.crawler.EnextCrawler;
import com.huythanh0x.udemycoupons.crawler_runner.crawler.RealDiscountCrawler;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.model.coupon.ExpiredCourseData;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import com.huythanh0x.udemycoupons.repository.ExpiredCouponRepository;
import com.huythanh0x.udemycoupons.utils.LastFetchTimeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    CouponCourseRepository couponCourseRepository;
    ExpiredCouponRepository expiredCouponRepository;
    EnextCrawler enextCrawler;
    RealDiscountCrawler realDiscountCrawler;
    Integer intervalTime;
    @Value("${custom.number-of-request-thread}")
    Integer numberOfThread;
    @Value("${custom.recheck-expired-days:3}")
    Integer recheckExpiredDays;
    @Value("${custom.refresh-expiring-hours:2}")
    Integer refreshExpiringHours;
    @Value("${custom.refresh-min-uses-remaining:50}")
    Integer refreshMinUsesRemaining;
    @Value("${custom.enable-smart-refresh:true}")
    Boolean enableSmartRefresh;
    @Value("${custom.batch-processing-size:100}")
    Integer batchProcessingSize;

    /**
     * Executes the method to start the crawler when the application runs.
     *
     * @param args The arguments passed to the application upon execution.
     */
    @Override
    public void run(ApplicationArguments args) {
        startCrawler();
    }

    public CrawlerRunner(CouponCourseRepository couponCourseRepository, ExpiredCouponRepository expiredCouponRepository, EnextCrawler enextCrawler, RealDiscountCrawler realDiscountCrawler, @Value("${custom.interval-time}") Integer intervalTime) {
        this.couponCourseRepository = couponCourseRepository;
        this.expiredCouponRepository = expiredCouponRepository;
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
                    
                    Set<String> couponsNeedingRefresh = new HashSet<>();
                    if (enableSmartRefresh) {
                        Instant expirationThreshold = Instant.now().plusSeconds(refreshExpiringHours * 3600L);
                        couponsNeedingRefresh = couponCourseRepository.findCouponUrlsNeedingRefresh(
                            expirationThreshold, 
                            refreshMinUsesRemaining
                        );
                        System.out.println("Found " + couponsNeedingRefresh.size() + 
                                         " coupons needing refresh (expiring within " + refreshExpiringHours + 
                                         " hours or uses remaining < " + refreshMinUsesRemaining + ")");
                    }
                    
                    Set<String> allUrlsToProcess = new HashSet<>(scrapedCouponUrls);
                    allUrlsToProcess.addAll(couponsNeedingRefresh);
                    
                    List<String> filterCouponUrls = filterValidCouponUrls(new ArrayList<>(allUrlsToProcess));
                    
                    saveAllCouponData(filterCouponUrls, numberOfThread);
                    delayUntilTheNextRound(startTime.get());
                }
            } catch (InterruptedException e) {
                System.out.println(e);
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
        System.out.println("\u001B[32mWait for " + delayTime + " milliseconds until the next run\u001B[32m");
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
        System.out.println("Processing " + allCouponUrls.size() + " URLs in batches of " + batchProcessingSize);
        
        Set<String> allExpiredCouponUrls = new HashSet<>();
        int totalProcessed = 0;
        
        while (totalProcessed < allCouponUrls.size()) {
            List<String> batch = allCouponUrls.stream()
                    .skip(totalProcessed)
                    .limit(batchProcessingSize)
                    .collect(Collectors.toList());
            
            System.out.println("Processing batch: " + (totalProcessed + 1) + "-" + 
                             Math.min(totalProcessed + batchProcessingSize, allCouponUrls.size()) + 
                             " of " + allCouponUrls.size());
            
            BatchResult batchResult = processBatch(batch, numberOfThread);
            
            if (!batchResult.validCoupons.isEmpty()) {
                couponCourseRepository.saveAll(batchResult.validCoupons);
                System.out.println("✓ Saved " + batchResult.validCoupons.size() + " valid coupons to database");
            }

            if (!batchResult.expiredCouponUrls.isEmpty()) {
                List<ExpiredCourseData> batchExpired = batchResult.expiredCouponUrls.stream()
                        .map(ExpiredCourseData::new)
                        .collect(Collectors.toList());
                expiredCouponRepository.saveAll(batchExpired);
                allExpiredCouponUrls.addAll(batchResult.expiredCouponUrls);
                System.out.println("✓ Saved " + batchResult.expiredCouponUrls.size() + " expired coupons to database");
            }

            if (!batchResult.failedToValidateCouponUrls.isEmpty()) {
                System.out.println("⚠ " + batchResult.failedToValidateCouponUrls.size() + " URLs failed to validate");
            }
            
            totalProcessed += batch.size();
            System.out.println("Progress: " + totalProcessed + "/" + allCouponUrls.size() + " URLs processed\n");
        }
        
        if (!allExpiredCouponUrls.isEmpty()) {
            couponCourseRepository.deleteAllCouponsByUrl(allExpiredCouponUrls);
            System.out.println("✓ Cleaned up " + allExpiredCouponUrls.size() + " expired coupons from main table");
        }
        
        System.out.println("All batches finished. Total processed: " + totalProcessed);
        LastFetchTimeManager.dumpFetchedTimeJsonToFile();
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
        Set<String> expiredCouponUrls = Collections.synchronizedSet(new HashSet<>());
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
        for (String couponUrl : batch) {
            executor.submit(() -> {
                try {
                    CouponCourseData couponCodeData = new UdemyCouponCourseExtractor(couponUrl).getFullCouponCodeData();
                    if (couponCodeData != null) {
                        validCoupons.add(couponCodeData);
                        System.out.println(couponCodeData.getTitle());
                    } else {
                        expiredCouponUrls.add(couponUrl);
                    }
                } catch (Exception e) {
                    failedToValidateCouponUrls.add(couponUrl + " " + e);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait until all threads are finished
        }
        
        return new BatchResult(validCoupons, failedToValidateCouponUrls, expiredCouponUrls);
    }

    /**
     * Helper class to hold batch processing results.
     */
    private static class BatchResult {
        final Set<CouponCourseData> validCoupons;
        final Set<String> failedToValidateCouponUrls;
        final Set<String> expiredCouponUrls;

        BatchResult(Set<CouponCourseData> validCoupons, Set<String> failedToValidateCouponUrls, Set<String> expiredCouponUrls) {
            this.validCoupons = validCoupons;
            this.failedToValidateCouponUrls = failedToValidateCouponUrls;
            this.expiredCouponUrls = expiredCouponUrls;
        }
    }

    /**
     * Filters coupon URLs using smart pre-validation
     * - Skips URLs already in DB (and still valid)
     * - Re-checks recently expired coupons (last N days) as they may be reactivated
     * - Skips old expired coupons (> N days)
     * 
     * This reduces API calls by avoiding unnecessary fetches for URLs we already know about.
     *
     * @param newCouponUrls the list of new coupon URLs scraped from crawlers
     * @return a list of coupon URLs that need to be fetched and validated
     */
    private List<String> filterValidCouponUrls(List<String> newCouponUrls) {
        if (newCouponUrls == null || newCouponUrls.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> existingCouponUrls = couponCourseRepository.findAllCouponUrls();
        
        Set<String> allExpiredUrls = expiredCouponRepository.findAllCouponUrls();
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(recheckExpiredDays);
        Timestamp sinceTimestamp = Timestamp.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant());
        
        Set<String> recentlyExpiredUrls = expiredCouponRepository.findCouponUrlsExpiredInLastDays(sinceTimestamp);
        return newCouponUrls.stream()
                .filter(couponUrl -> couponUrl != null && !couponUrl.trim().isEmpty())
                .filter(couponUrl -> {
                    if (existingCouponUrls.contains(couponUrl)) {
                        return false;
                    }
                    if (recentlyExpiredUrls.contains(couponUrl)) {
                        return true;
                    }
                    if (allExpiredUrls.contains(couponUrl)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
