package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.crawler.EnextCrawler;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.crawler.RealDiscountCrawler;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.ExpiredCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.CouponCourseRepository;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.ExpiredCouponRepository;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.utils.LastFetchTimeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ComponentScan("com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository")
public class CrawlerRunner implements ApplicationRunner {
    CouponCourseRepository couponCourseRepository;
    ExpiredCouponRepository expiredCouponRepository;
    EnextCrawler enextCrawler;
    RealDiscountCrawler realDiscountCrawler;
    Integer intervalTime;
    @Value("${custom.number-of-request-thread}")
    Integer numberOfThread;

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

    private void delayUntilTheNextRound(long startTime) throws InterruptedException {
        long runTime = System.currentTimeMillis() - startTime;
        long delayTime = Math.max(intervalTime - runTime, 0);
        System.out.println("\u001B[32mWait for " + delayTime + " milliseconds until the next run\u001B[32m");
        Thread.sleep(delayTime);
    }

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

    private void dumpDataToTheDatabase(Set<CouponCourseData> validCoupons, Set<String> failedToValidateCouponUrls, Set<String> expiredCouponUrls) {
        List<ExpiredCourseData> allExpiredCourses = expiredCouponUrls.stream().map(ExpiredCourseData::new).collect(Collectors.toList());
        expiredCouponRepository.saveAll(allExpiredCourses);
        couponCourseRepository.deleteAllCouponsByUrl(expiredCouponUrls);
        couponCourseRepository.saveAll(validCoupons);
    }

    private List<String> filterValidCouponUrls(List<String> newCouponUrls) {
        List<String> allOldCoupons = couponCourseRepository.findAll().stream().map(CouponCourseData::getCouponUrl).toList();
        List<ExpiredCourseData> allExpiredCoupons = expiredCouponRepository.findAll();
        Stream<String> combinedStreams = Stream.concat(allOldCoupons.stream(), newCouponUrls.stream());
        return combinedStreams.filter(couponUrl -> couponUrl != null && !allExpiredCoupons.stream().map(ExpiredCourseData::getCouponUrl).toString().contains(couponUrl)).collect(Collectors.toList());
    }
}
