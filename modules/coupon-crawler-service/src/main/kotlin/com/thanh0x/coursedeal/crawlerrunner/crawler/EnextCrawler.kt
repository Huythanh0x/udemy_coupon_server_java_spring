package com.thanh0x.coursedeal.crawlerrunner.crawler

import com.thanh0x.coursedeal.config.CrawlerProperties
import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.crawlerrunner.base.CouponUrlCrawlerBase
import com.thanh0x.coursedeal.crawlerrunner.fetcher.WebContentFetcher
import com.thanh0x.coursedeal.model.coupon.ScrapedUrlMapping
import com.thanh0x.coursedeal.repository.ScrapedUrlMappingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.nodes.Element
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 * Component class for crawling coupon URLs from the Enext site which now renders server-side HTML.
 * Extends CouponUrlCrawlerBase.
 */
@Component
class EnextCrawler(
    private val properties: CrawlerProperties,
    private val scrapedUrlMappingRepository: ScrapedUrlMappingRepository,
) : CouponUrlCrawlerBase() {
    private val log = logger()

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

        return when {
            scrapedUrl.startsWith("https://www.udemy.com") || scrapedUrl.startsWith("https://udemy.com/") ->
                scrapedUrl
            else ->
                scrapedUrlMappingRepository.findByScrapedUrl(scrapedUrl)?.couponUrl
                    ?: extractCouponUrlFromDetailPage(scrapedUrl)
        }
    }

    private fun extractCouponUrlFromDetailPage(scrapedUrl: String): String? =
        try {
            val fetcher = WebContentFetcher()
            val detailDoc = fetcher.getHtmlDocumentFrom(scrapedUrl)

            val couponAnchor =
                detailDoc?.selectFirst("a.btn.btn-primary[href*='udemy.com']")
                    ?: detailDoc?.selectFirst("a[href*='udemy.com/?couponCode=']")

            couponAnchor?.attr("href")?.trim()?.takeIf { it.isNotEmpty() }?.also { udemyUrl ->
                val mapping =
                    ScrapedUrlMapping(
                        scrapedUrl = scrapedUrl,
                        couponUrl = udemyUrl,
                        crawlerSource = CRAWLER_SOURCE,
                    )
                scrapedUrlMappingRepository.save(mapping)
            }
        } catch (e: DataAccessException) {
            log.warn("Error mapping scraped URL {} to coupon URL: {}", scrapedUrl, e.message)
            null
        }

    /**
     * Retrieves all coupon URLs using coroutines: list pages are fetched concurrently and their
     * detail-page URLs are streamed through a channel to a pool of consumers that resolve them
     * to Udemy coupon URLs.
     */
    override fun getAllCouponUrls(): List<String> =
        runBlocking {
            val estimatedPages = estimatedPageCount()
            val allUrls = mutableListOf<String>()
            val mutex = Mutex()
            val collectedCount = AtomicInteger(0)
            val detailUrlChannel = Channel<String>(Channel.UNLIMITED)

            coroutineScope {
                launch(Dispatchers.IO) {
                    val producers =
                        (1..estimatedPages).map { page ->
                            launch { fetchListPage(page, collectedCount, detailUrlChannel) }
                        }
                    producers.joinAll()
                    detailUrlChannel.close()
                }

                repeat(properties.numberOfRequestThread) {
                    launch(Dispatchers.IO) {
                        consumeDetailUrls(detailUrlChannel, collectedCount, mutex, allUrls)
                    }
                }
            }

            capAtConfiguredLimit(allUrls)
        }

    private fun estimatedPageCount(): Int {
        val pagesForRequestedCount = kotlin.math.ceil(properties.numberOfEnextCoupon.toDouble() / COUPON_PER_PAGE)
        return pagesForRequestedCount.toInt() + 2
    }

    private suspend fun fetchListPage(
        page: Int,
        collectedCount: AtomicInteger,
        detailUrlChannel: Channel<String>,
    ) {
        if (collectedCount.get() >= properties.numberOfEnextCoupon) return
        try {
            val fetcher = WebContentFetcher()
            val listUrl = String.format(LIST_PAGE_FORMAT, page)
            val courseAnchors = fetcher.getHtmlDocumentFrom(listUrl)?.select("div.portfolio-item a[href]") ?: return

            for (anchor in courseAnchors) {
                if (collectedCount.get() >= properties.numberOfEnextCoupon) break
                sendDetailUrlIfPresent(anchor, detailUrlChannel)
            }
            log.info("Enext page {} processed, collected so far {}", page, collectedCount.get())
        } catch (e: ClosedSendChannelException) {
            log.warn("Error processing list page {}: {}", page, e.message)
        }
    }

    private suspend fun sendDetailUrlIfPresent(
        anchor: Element,
        detailUrlChannel: Channel<String>,
    ) {
        val href = anchor.attr("href").trim()
        if (href.isEmpty()) return
        val detailUrl =
            if (href.startsWith("http")) {
                href
            } else {
                SITE_BASE + (if (href.startsWith("/")) href else "/$href")
            }
        detailUrlChannel.send(detailUrl)
    }

    private suspend fun consumeDetailUrls(
        detailUrlChannel: Channel<String>,
        collectedCount: AtomicInteger,
        mutex: Mutex,
        allUrls: MutableList<String>,
    ) {
        for (detailUrl in detailUrlChannel) {
            if (collectedCount.get() >= properties.numberOfEnextCoupon) break

            val udemyUrl = mapScrapedUrlToCouponUrl(detailUrl)
            if (!udemyUrl.isNullOrEmpty()) {
                mutex.withLock {
                    if (collectedCount.get() < properties.numberOfEnextCoupon) {
                        allUrls.add(udemyUrl)
                        collectedCount.incrementAndGet()
                    }
                }
            }
        }
    }

    private fun capAtConfiguredLimit(allUrls: List<String>): List<String> =
        if (allUrls.size > properties.numberOfEnextCoupon) {
            allUrls.subList(0, properties.numberOfEnextCoupon)
        } else {
            allUrls
        }
}
