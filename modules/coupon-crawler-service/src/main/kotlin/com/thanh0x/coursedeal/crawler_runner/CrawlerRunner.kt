package com.thanh0x.coursedeal.crawler_runner

import com.thanh0x.coursedeal.config.CrawlerProperties
import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.crawler_runner.crawler.EnextCrawler
import com.thanh0x.coursedeal.crawler_runner.crawler.RealDiscountCrawler
import com.thanh0x.coursedeal.repository.CouponCourseRepository
import com.thanh0x.coursedeal.service.CourseScraperService
import com.thanh0x.coursedeal.utils.LastFetchTimeManager
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

/**
 * CrawlerRunner class is responsible for running the web crawlers to fetch coupon URLs
 * and handing them off to the CourseScraperService for async processing.
 */
@Component
@EnableConfigurationProperties(CrawlerProperties::class)
class CrawlerRunner(
    private val couponCourseRepository: CouponCourseRepository,
    private val courseScraperService: CourseScraperService,
    private val enextCrawler: EnextCrawler,
    private val realDiscountCrawler: RealDiscountCrawler,
    private val properties: CrawlerProperties,
) : ApplicationRunner {
    private val log = logger()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun run(args: ApplicationArguments) {
        startCrawler()
    }

    /**
     * Starts the crawler process that continuously fetches coupon URLs.
     */
    fun startCrawler() {
        val lastFetchedTime = LastFetchTimeManager.loadLasFetchedTimeInMilliSecond()
        val startTime = AtomicLong(lastFetchedTime)

        scope.launch {
            try {
                delayUntilTheNextRound(startTime.get())
                while (isActive) {
                    startTime.set(System.currentTimeMillis())

                    log.info("Starting new crawl round...")
                    val discoveredUrls = mutableListOf<String>()
                    discoveredUrls.addAll(enextCrawler.getAllCouponUrls())
                    discoveredUrls.addAll(realDiscountCrawler.getAllCouponUrls())

                    val uniqueUrls = discoveredUrls.toSet()
                    log.info("Discovered {} unique URLs. Handing off to background scraper...", uniqueUrls.size)

                    for (url in uniqueUrls) {
                        courseScraperService.enqueueScrapingTask(url, "crawler")
                    }

                    log.info("Crawl round finished. Handed off {} tasks.", uniqueUrls.size)
                    LastFetchTimeManager.updateLastBulkRefreshCoupon()

                    delayUntilTheNextRound(startTime.get())
                }
            } catch (e: CancellationException) {
                log.info("Crawler coroutine cancelled")
            } catch (e: Exception) {
                log.error("Error in crawler loop", e)
            }
        }
    }

    private suspend fun delayUntilTheNextRound(startTime: Long) {
        val runTime = System.currentTimeMillis() - startTime
        val delayTime = (properties.intervalTime - runTime).coerceAtLeast(0)
        log.info("Waiting {} ms until the next run", delayTime)
        delay(delayTime)
    }

    @PreDestroy
    fun stop() {
        scope.cancel()
    }
}
