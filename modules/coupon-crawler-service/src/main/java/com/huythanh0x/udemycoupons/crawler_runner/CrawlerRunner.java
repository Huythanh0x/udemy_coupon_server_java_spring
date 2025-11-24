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

import java.time.Instant;
import java.time.LocalDateTime;
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
                    
                    Set<String> allCouponsNeedingRefresh = new HashSet<>();
                    Instant expirationThreshold = Instant.now().plusSeconds(refreshExpiringHours * 3600L);
                    LocalDateTime updatedBefore = LocalDateTime.now().minusHours(refreshOldHours);
                    
                    allCouponsNeedingRefresh = couponCourseRepository.findCouponUrlsNeedingRefresh(
                        expirationThreshold, 
                        refreshMinUsesRemaining,
                        updatedBefore
                    );
                    System.out.println("Found " + allCouponsNeedingRefresh.size() + 
                                     " coupons needing refresh (expiring within " + refreshExpiringHours + 
                                     " hours, uses remaining < " + refreshMinUsesRemaining + 
                                     ", or not updated in last " + refreshOldHours + " hours)");
                    
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
                
                System.out.println("✓ Saved " + batchResult.validCoupons.size() + " valid coupons to database");
            }

            if (!batchResult.expiredCouponUrls.isEmpty()) {
                Set<String> existingExpiredUrls = new HashSet<>();
                List<ExpiredCourseData> expiredToCreate = new ArrayList<>();
                
                for (String expiredUrl : batchResult.expiredCouponUrls) {
                    ExpiredCourseData existingExpired = expiredCouponRepository.findByCouponUrl(expiredUrl);
                    if (existingExpired != null) {
                        existingExpiredUrls.add(expiredUrl);
                    } else {
                        expiredToCreate.add(new ExpiredCourseData(expiredUrl));
                    }
                }
                
                if (!existingExpiredUrls.isEmpty()) {
                    expiredCouponRepository.updateUpdatedAtForUrls(existingExpiredUrls);
                }
                
                if (!expiredToCreate.isEmpty()) {
                    expiredCouponRepository.saveAll(expiredToCreate);
                }
                
                allExpiredCouponUrls.addAll(batchResult.expiredCouponUrls);
                System.out.println("✓ Saved/Updated " + batchResult.expiredCouponUrls.size() + 
                                 " expired coupons to database (" + expiredToCreate.size() + " new, " + 
                                 existingExpiredUrls.size() + " updated)");
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
