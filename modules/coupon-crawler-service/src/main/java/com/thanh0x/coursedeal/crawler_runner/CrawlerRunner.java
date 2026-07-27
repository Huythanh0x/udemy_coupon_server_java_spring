package com.thanh0x.coursedeal.crawler_runner;

import com.thanh0x.coursedeal.crawler_runner.crawler.EnextCrawler;
import com.thanh0x.coursedeal.crawler_runner.crawler.RealDiscountCrawler;
import com.thanh0x.coursedeal.repository.CouponCourseRepository;
import com.thanh0x.coursedeal.service.CourseScraperService;
import com.thanh0x.coursedeal.utils.LastFetchTimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CrawlerRunner class is responsible for running the web crawlers to fetch coupon URLs
 * and handing them off to the CourseScraperService for async processing.
 */
@Component
public class CrawlerRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CrawlerRunner.class);
    
    private final CouponCourseRepository couponCourseRepository;
    private final CourseScraperService courseScraperService;
    private final EnextCrawler enextCrawler;
    private final RealDiscountCrawler realDiscountCrawler;
    private final Integer intervalTime;

    public CrawlerRunner(CouponCourseRepository couponCourseRepository,
                         CourseScraperService courseScraperService,
                         EnextCrawler enextCrawler,
                         RealDiscountCrawler realDiscountCrawler,
                         @Value("${custom.interval-time}") Integer intervalTime) {
        this.couponCourseRepository = couponCourseRepository;
        this.courseScraperService = courseScraperService;
        this.enextCrawler = enextCrawler;
        this.realDiscountCrawler = realDiscountCrawler;
        this.intervalTime = intervalTime;
    }

    @Override
    public void run(ApplicationArguments args) {
        startCrawler();
    }

    /**
     * Starts the crawler process that continuously fetches coupon URLs
     * and hands them off to the background scraper.
     */
    public void startCrawler() {
        AtomicLong startTime = new AtomicLong(LastFetchTimeManager.loadLasFetchedTimeInMilliSecond());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                delayUntilTheNextRound(startTime.get());
                while (true) {
                    startTime.set(System.currentTimeMillis());
                    
                    log.info("Starting new crawl round...");
                    List<String> discoveredUrls = new ArrayList<>();
                    discoveredUrls.addAll(enextCrawler.getAllCouponUrls());
                    discoveredUrls.addAll(realDiscountCrawler.getAllCouponUrls());
                    
                    Set<String> uniqueUrls = new HashSet<>(discoveredUrls);
                    log.info("Discovered {} unique URLs. Handing off to background scraper...", uniqueUrls.size());
                    
                    for (String url : uniqueUrls) {
                        courseScraperService.validateAndSaveCouponAsync(url, "crawler");
                    }
                    
                    log.info("Crawl round finished. Handed off {} tasks.", uniqueUrls.size());
                    LastFetchTimeManager.updateLastBulkRefreshCoupon();
                    
                    delayUntilTheNextRound(startTime.get());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Crawler thread interrupted", e);
            }
        });
    }

    private void delayUntilTheNextRound(long startTime) throws InterruptedException {
        long runTime = System.currentTimeMillis() - startTime;
        long delayTime = Math.max(intervalTime - runTime, 0);
        log.info("Waiting {} ms until the next run", delayTime);
        Thread.sleep(delayTime);
    }
}
