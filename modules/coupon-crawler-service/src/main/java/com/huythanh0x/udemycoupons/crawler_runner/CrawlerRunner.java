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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
                    List<String> allCouponUrls = new ArrayList<>();
                    allCouponUrls.addAll(enextCrawler.getAllCouponUrls());
                    allCouponUrls.addAll(realDiscountCrawler.getAllCouponUrls());
                    List<String> filterCouponUrls = filterValidCouponUrls(allCouponUrls);
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
     * Saves all coupon data by extracting the full coupon code data for each coupon URL provided in the list.
     *
     * @param allCouponUrls List of URLs containing coupon data to be processed
     * @param numberOfThread Number of threads to execute concurrently for processing the data
     */
    private void saveAllCouponData(List<String> allCouponUrls, int numberOfThread) {
        Set<CouponCourseData> validCoupons = new HashSet<>();
        Set<String> failedToValidateCouponUrls = new HashSet<>();
        Set<String> expiredCouponUrls = new HashSet<>();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
        for (String couponUrl : allCouponUrls) {
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
        System.out.println("All threads finished");
        LastFetchTimeManager.dumpFetchedTimeJsonToFile();
        dumpDataToTheDatabase(validCoupons, failedToValidateCouponUrls, expiredCouponUrls);
    }

    /**
     * Dumps data to the database by saving expired courses, deleting expired coupons,
     * and saving valid coupons.
     *
     * @param validCoupons               Set of valid CouponCourseData to be saved
     * @param failedToValidateCouponUrls Set of URLs of coupons that failed to validate
     * @param expiredCouponUrls         Set of URLs of expired coupons
     */
    private void dumpDataToTheDatabase(Set<CouponCourseData> validCoupons, Set<String> failedToValidateCouponUrls, Set<String> expiredCouponUrls) {
        List<ExpiredCourseData> allExpiredCourses = expiredCouponUrls.stream().map(ExpiredCourseData::new).collect(Collectors.toList());
        expiredCouponRepository.saveAll(allExpiredCourses);
        couponCourseRepository.deleteAllCouponsByUrl(expiredCouponUrls);
        couponCourseRepository.saveAll(validCoupons);
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
