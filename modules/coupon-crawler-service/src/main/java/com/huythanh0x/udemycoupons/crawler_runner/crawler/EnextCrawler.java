// java
package com.huythanh0x.udemycoupons.crawler_runner.crawler;

import com.huythanh0x.udemycoupons.crawler_runner.base.CouponUrlCrawlerBase;
import com.huythanh0x.udemycoupons.crawler_runner.fetcher.WebContentFetcher;
import com.huythanh0x.udemycoupons.model.coupon.ScrapedUrlMapping;
import com.huythanh0x.udemycoupons.repository.ScrapedUrlMappingRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Component class for crawling coupon URLs from the Enext site which now renders server-side HTML.
 * Extends CouponUrlCrawlerBase.
 */
@Component
public class EnextCrawler extends CouponUrlCrawlerBase {
    private static final Logger log = LoggerFactory.getLogger(EnextCrawler.class);
    private static final String LIST_PAGE_FORMAT = "https://jobs.e-next.in/course/udemy/%d";
    private static final String SITE_BASE = "https://jobs.e-next.in";
    private static final Integer COUPON_PER_PAGE = 12;

    private final int maxCouponRequest;
    private final int numberOfThreads;
    private final ScrapedUrlMappingRepository scrapedUrlMappingRepository;
    private static final String CRAWLER_SOURCE = "enext";

    EnextCrawler(
            @Value("${custom.number-of-enext-coupon}") int maxCouponRequest,
            @Value("${custom.number-of-request-thread}") int numberOfThreads,
            ScrapedUrlMappingRepository scrapedUrlMappingRepository) {
        this.maxCouponRequest = maxCouponRequest;
        this.numberOfThreads = numberOfThreads;
        this.scrapedUrlMappingRepository = scrapedUrlMappingRepository;
    }

    /**
     * Maps a scraped Enext detail page URL to its corresponding Udemy coupon URL.
     * First checks the mapping repository cache to avoid unnecessary API calls.
     * If not found in cache, fetches the detail page and extracts the coupon URL.
     * 
     * This is an internal method used only within EnextCrawler.
     *
     * @param scrapedUrl The Enext detail page URL
     * @return The Udemy coupon URL, or null if extraction fails
     */
    private String mapScrapedUrlToCouponUrl(String scrapedUrl) {
        if (scrapedUrl == null || scrapedUrl.trim().isEmpty()) {
            return null;
        }
        
        if (scrapedUrl.startsWith("https://www.udemy.com") || scrapedUrl.startsWith("https://udemy.com/")) {
            return scrapedUrl;
        }
        
        ScrapedUrlMapping existingMapping = scrapedUrlMappingRepository.findByScrapedUrl(scrapedUrl);
        if (existingMapping != null) {
            return existingMapping.getCouponUrl();
        }
        
        return extractCouponUrlFromDetailPage(scrapedUrl);
    }

    private String extractCouponUrlFromDetailPage(String scrapedUrl) {
        try {
            WebContentFetcher fetcher = new WebContentFetcher();
            Document detailDoc = fetcher.getHtmlDocumentFrom(scrapedUrl);
            if (detailDoc == null) {
                return null;
            }
            
            Element couponAnchor = detailDoc.selectFirst("a.btn.btn-primary[href*='udemy.com']");
            if (couponAnchor == null) {
                couponAnchor = detailDoc.selectFirst("a[href*='udemy.com/?couponCode=']");
            }
            
            if (couponAnchor != null) {
                String udemyUrl = couponAnchor.attr("href").trim();
                if (!udemyUrl.isEmpty()) {
                    ScrapedUrlMapping mapping = ScrapedUrlMapping.builder()
                        .scrapedUrl(scrapedUrl)
                        .couponUrl(udemyUrl)
                        .crawlerSource(CRAWLER_SOURCE)
                        .build();
                    scrapedUrlMappingRepository.save(mapping);
                    return udemyUrl;
                }
            }
        } catch (Exception e) {
            log.warn("Error mapping scraped URL {} to coupon URL: {}", scrapedUrl, e.getMessage());
        }
        return null;
    }
    
    /**
     * Retrieves all coupon URLs using a producer-consumer pattern:
     * - Producers: Fetch list pages concurrently and extract detail URLs
     * - Consumers: Fetch detail pages concurrently as soon as URLs are available
     * This minimizes waiting time by starting detail fetching immediately when URLs become available.
     *
     * @return a list of Udemy coupon URLs
     */
    @Override
    public List<String> getAllCouponUrls() {
        int estimatedPages = (int) Math.ceil((double) maxCouponRequest / COUPON_PER_PAGE) + 2;
        
        List<String> allUrls = Collections.synchronizedList(new ArrayList<>());
        BlockingQueue<String> detailUrlQueue = new LinkedBlockingQueue<>();
        AtomicInteger collectedCount = new AtomicInteger(0);
        AtomicInteger listPagesProcessed = new AtomicInteger(0);
        AtomicBoolean allListPagesDone = new AtomicBoolean(false);
        AtomicBoolean enoughCouponsCollected = new AtomicBoolean(false);
        
        // Thread pools: one for list pages (producers), one for detail pages (consumers)
        ExecutorService listPageExecutor = Executors.newFixedThreadPool(Math.min(numberOfThreads, estimatedPages));
        ExecutorService detailPageExecutor = Executors.newFixedThreadPool(numberOfThreads);
        
        int detailConsumerCount = numberOfThreads;
        CountDownLatch detailConsumersLatch = new CountDownLatch(detailConsumerCount);
        
        for (int i = 0; i < detailConsumerCount; i++) {
            detailPageExecutor.submit(() -> {
                try {
                    WebContentFetcher fetcher = new WebContentFetcher();
                    while (!enoughCouponsCollected.get() && (!allListPagesDone.get() || !detailUrlQueue.isEmpty())) {
                        String currentDetailUrl = null;
                        try {
                            // Poll with timeout to allow checking conditions
                            currentDetailUrl = detailUrlQueue.poll(1, TimeUnit.SECONDS);
                            if (currentDetailUrl == null) {
                                continue; // Timeout, check conditions again
                            }
                            
                            if (collectedCount.get() >= maxCouponRequest) {
                                enoughCouponsCollected.set(true);
                                break;
                            }
                            
                            String udemyUrl = mapScrapedUrlToCouponUrl(currentDetailUrl);
                            if (udemyUrl != null && !udemyUrl.isEmpty()) {
                                synchronized (allUrls) {
                                    if (collectedCount.get() < maxCouponRequest) {
                                        allUrls.add(udemyUrl);
                                        int current = collectedCount.incrementAndGet();
                                        if (current >= maxCouponRequest) {
                                            enoughCouponsCollected.set(true);
                                        }
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.warn("Detail consumer interrupted", e);
                            break;
                        } catch (Exception e) {
                            log.warn("Error processing detail page {}: {}", currentDetailUrl, e.getMessage());
                        }
                    }
                } finally {
                    detailConsumersLatch.countDown();
                }
            });
        }
        
        // Start list page producers concurrently
        try {
            for (int page = 1; page <= estimatedPages && !enoughCouponsCollected.get(); page++) {
                final int currentPage = page;
                listPageExecutor.submit(() -> {
                    try {
                        if (enoughCouponsCollected.get()) {
                            return;
                        }
                        
                        WebContentFetcher fetcher = new WebContentFetcher();
                        String listUrl = String.format(LIST_PAGE_FORMAT, currentPage);
                        Document listDoc = fetcher.getHtmlDocumentFrom(listUrl);
                        
                        if (listDoc == null) {
                            log.warn("Failed to fetch list page: {}", listUrl);
                            // Mark as done if this is the first page (site might be down)
                            if (currentPage == 1) {
                                allListPagesDone.set(true);
                            }
                            return;
                        }
                        
                        Elements courseAnchors = listDoc.select("div.portfolio-item a[href]");
                        if (courseAnchors.isEmpty()) {
                            // No items on this page - we've reached the end
                            allListPagesDone.set(true);
                            log.info("Enext page {} is empty, stopping list page fetching", currentPage);
                            return;
                        }
                        
                        List<String> detailUrlsThisPage = new ArrayList<>();
                        for (Element a : courseAnchors) {
                            if (enoughCouponsCollected.get()) {
                                break;
                            }
                            String href = a.attr("href").trim();
                            if (href.isEmpty()) continue;
                            String detailUrl = href.startsWith("http") ? href : SITE_BASE + (href.startsWith("/") ? href : ("/" + href));
                            detailUrlsThisPage.add(detailUrl);
                        }
                        
                        // Add detail URLs to queue for immediate processing
                        for (String detailUrl : detailUrlsThisPage) {
                            if (enoughCouponsCollected.get()) {
                                break;
                            }
                            try {
                                detailUrlQueue.put(detailUrl);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.warn("Interrupted while queueing detail URL {}", detailUrl, e);
                                return;
                            }
                        }
                        
                        int processed = listPagesProcessed.incrementAndGet();
                        log.info("Enext page {} processed: {} detail URLs queued, collected so far {}",
                                currentPage, detailUrlsThisPage.size(), collectedCount.get());
                        
                    } catch (Exception e) {
                        log.warn("Error processing list page {}: {}", currentPage, e.getMessage());
                    }
                });
            }
        } finally {
            // Shutdown list page executor and wait for completion
            listPageExecutor.shutdown();
            try {
                if (!listPageExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    listPageExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for list page executor shutdown", e);
                listPageExecutor.shutdownNow();
            }
            
            allListPagesDone.set(true);
            
            try {
                detailConsumersLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for detail consumers latch", e);
            }
            
            detailPageExecutor.shutdown();
            try {
                if (!detailPageExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    detailPageExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for detail page executor shutdown", e);
                detailPageExecutor.shutdownNow();
            }
        }
        
        if (allUrls.size() > maxCouponRequest) {
            return allUrls.subList(0, maxCouponRequest);
        }
        return new ArrayList<>(allUrls);
    }
}
