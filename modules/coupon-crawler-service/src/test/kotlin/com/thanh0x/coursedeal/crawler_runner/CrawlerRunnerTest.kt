package com.thanh0x.coursedeal.crawler_runner

import com.thanh0x.coursedeal.config.CrawlerProperties
import com.thanh0x.coursedeal.crawler_runner.crawler.EnextCrawler
import com.thanh0x.coursedeal.crawler_runner.crawler.RealDiscountCrawler
import com.thanh0x.coursedeal.repository.CouponCourseRepository
import com.thanh0x.coursedeal.service.CourseScraperService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * startCrawler() launches a background coroutine rather than running synchronously, so these
 * tests give it a moment to complete its first round instead of driving it directly. The
 * default intervalTime (900_000ms) keeps a second round from starting during that wait.
 */
class CrawlerRunnerTest {
    private val couponCourseRepository = mock(CouponCourseRepository::class.java)
    private val courseScraperService = mock(CourseScraperService::class.java)
    private val enextCrawler = mock(EnextCrawler::class.java)
    private val realDiscountCrawler = mock(RealDiscountCrawler::class.java)
    private val properties = CrawlerProperties()

    private val runner =
        CrawlerRunner(
            couponCourseRepository,
            courseScraperService,
            enextCrawler,
            realDiscountCrawler,
            properties,
        )

    @AfterEach
    fun tearDown() {
        runner.stop()
    }

    @Test
    fun `startCrawler discovers urls from both crawlers, dedupes overlap, and enqueues each exactly once`() {
        `when`(enextCrawler.getAllCouponUrls()).thenReturn(listOf("https://a.example/1", "https://b.example/2"))
        `when`(realDiscountCrawler.getAllCouponUrls()).thenReturn(listOf("https://b.example/2", "https://c.example/3"))

        runner.startCrawler()
        Thread.sleep(500)

        verify(courseScraperService, times(1)).enqueueScrapingTask("https://a.example/1", "crawler")
        verify(courseScraperService, times(1)).enqueueScrapingTask("https://b.example/2", "crawler")
        verify(courseScraperService, times(1)).enqueueScrapingTask("https://c.example/3", "crawler")
    }
}
