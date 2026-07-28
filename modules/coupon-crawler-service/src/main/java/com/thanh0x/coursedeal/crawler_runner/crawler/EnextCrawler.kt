package com.thanh0x.coursedeal.crawler_runner.crawler

import com.thanh0x.coursedeal.crawler_runner.base.CouponUrlCrawlerBase
import com.thanh0x.coursedeal.crawler_runner.fetcher.WebContentFetcher
import com.thanh0x.coursedeal.model.coupon.ScrapedUrlMapping
import com.thanh0x.coursedeal.repository.ScrapedUrlMappingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 * Component class for crawling coupon URLs from the Enext site which now renders server-side HTML.
 * Extends CouponUrlCrawlerBase.
 */
@Component
class EnextCrawler(
    @Value("\${custom.number-of-enext-coupon}") private val maxCouponRequest: Int,
    @Value("\${custom.number-of-request-thread}") private val numberOfThreads: Int,
    private val scrapedUrlMappingRepository: ScrapedUrlMappingRepository
) : CouponUrlCrawlerBase() {

    private val log = LoggerFactory.getLogger(EnextCrawler::class.java)

    companion object {
        private const val LIST_PAGE_FORMAT = "https://jobs.e-next.in/course/udemy/%d"
        private const val SITE_BASE = "https://jobs.e-next.in"
        private const val COUPON_PER_PAGE = 12
        private const val CRAWLER_SOURCE = "enext"
    }

    /**
     * Maps a scraped Enext detail page URL to its corresponding Udemy coupon URL.
     */
    private fun mapScrapedUrlToCouponUrl(scrapedUrl: String?): String? {
        if (scrapedUrl.isNullOrBlank()) return null

        if (scrapedUrl.startsWith("https://www.udemy.com") || scrapedUrl.startsWith("https://udemy.com/")) {
            return scrapedUrl
        }

        val existingMapping = scrapedUrlMappingRepository.findByScrapedUrl(scrapedUrl)
        if (existingMapping != null) {
            return existingMapping.couponUrl
        }

        return extractCouponUrlFromDetailPage(scrapedUrl)
    }

    private fun extractCouponUrlFromDetailPage(scrapedUrl: String): String? {
        return try {
            val fetcher = WebContentFetcher()
            val detailDoc = fetcher.getHtmlDocumentFrom(scrapedUrl) ?: return null

            val couponAnchor = detailDoc.selectFirst("a.btn.btn-primary[href*='udemy.com']")
                ?: detailDoc.selectFirst("a[href*='udemy.com/?couponCode=']")

            couponAnchor?.attr("href")?.trim()?.takeIf { it.isNotEmpty() }?.also { udemyUrl ->
                val mapping = ScrapedUrlMapping(
                    scrapedUrl = scrapedUrl,
                    couponUrl = udemyUrl,
                    crawlerSource = CRAWLER_SOURCE
                )
                scrapedUrlMappingRepository.save(mapping)
            }
        } catch (e: Exception) {
            log.warn("Error mapping scraped URL {} to coupon URL: {}", scrapedUrl, e.message)
            null
        }
    }

    /**
     * Retrieves all coupon URLs using coroutines.
     */
    override fun getAllCouponUrls(): List<String> = runBlocking {
        val estimatedPages = kotlin.math.ceil(maxCouponRequest.toDouble() / COUPON_PER_PAGE).toInt() + 2
        val allUrls = mutableListOf<String>()
        val mutex = Mutex()
        val collectedCount = AtomicInteger(0)
        val detailUrlChannel = Channel<String>(Channel.UNLIMITED)

        coroutineScope {
            // Producers: Fetch list pages
            launch(Dispatchers.IO) {
                for (page in 1..estimatedPages) {
                    if (collectedCount.get() >= maxCouponRequest) break
                    
                    launch {
                        try {
                            val fetcher = WebContentFetcher()
                            val listUrl = String.format(LIST_PAGE_FORMAT, page)
                            val listDoc = fetcher.getHtmlDocumentFrom(listUrl)

                            if (listDoc == null) {
                                log.warn("Failed to fetch list page: {}", listUrl)
                                if (page == 1) detailUrlChannel.close()
                                return@launch
                            }

                            val courseAnchors = listDoc.select("div.portfolio-item a[href]")
                            if (courseAnchors.isEmpty()) {
                                log.info("Enext page {} is empty, stopping list page fetching", page)
                                // Note: we can't easily close the channel here if other producers are still running
                                return@launch
                            }

                            for (a in courseAnchors) {
                                if (collectedCount.get() >= maxCouponRequest) break
                                val href = a.attr("href").trim()
                                if (href.isEmpty()) continue
                                val detailUrl = if (href.startsWith("http")) href 
                                                else SITE_BASE + (if (href.startsWith("/")) href else "/$href")
                                detailUrlChannel.send(detailUrl)
                            }
                            log.info("Enext page {} processed, collected so far {}", page, collectedCount.get())
                        } catch (e: Exception) {
                            log.warn("Error processing list page {}: {}", page, e.message)
                        }
                    }
                }
                // We need to wait for all list page producers to finish before closing the channel
                // But here they are launched as child coroutines of this launch.
            }.invokeOnCompletion { 
                // This doesn't wait for child coroutines.
            }

            // Consumers: Fetch detail pages
            repeat(numberOfThreads) {
                launch(Dispatchers.IO) {
                    for (detailUrl in detailUrlChannel) {
                        if (collectedCount.get() >= maxCouponRequest) break
                        
                        val udemyUrl = mapScrapedUrlToCouponUrl(detailUrl)
                        if (!udemyUrl.isNullOrEmpty()) {
                            mutex.withLock {
                                if (collectedCount.get() < maxCouponRequest) {
                                    allUrls.add(udemyUrl)
                                    collectedCount.incrementAndGet()
                                }
                            }
                        }
                    }
                }
            }
            
            // Wait for list pages to be processed and then close the channel
            // Improving the logic: use a separate scope or supervisor job to manage the producers
            launch(Dispatchers.IO) {
                val producers = (1..estimatedPages).map { page ->
                    launch {
                        if (collectedCount.get() >= maxCouponRequest) return@launch
                        try {
                            val fetcher = WebContentFetcher()
                            val listUrl = String.format(LIST_PAGE_FORMAT, page)
                            val listDoc = fetcher.getHtmlDocumentFrom(listUrl) ?: return@launch

                            val courseAnchors = listDoc.select("div.portfolio-item a[href]")
                            if (courseAnchors.isEmpty()) return@launch

                            for (a in courseAnchors) {
                                if (collectedCount.get() >= maxCouponRequest) break
                                val href = a.attr("href").trim()
                                if (href.isEmpty()) continue
                                val detailUrl = if (href.startsWith("http")) href 
                                                else SITE_BASE + (if (href.startsWith("/")) href else "/$href")
                                detailUrlChannel.send(detailUrl)
                            }
                        } catch (e: Exception) {
                            log.warn("Error processing list page {}: {}", page, e.message)
                        }
                    }
                }
                producers.joinAll()
                detailUrlChannel.close()
            }
        }

        if (allUrls.size > maxCouponRequest) allUrls.subList(0, maxCouponRequest) else allUrls
    }
}
