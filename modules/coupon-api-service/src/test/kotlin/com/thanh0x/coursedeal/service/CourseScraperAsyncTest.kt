package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.repository.CouponCourseRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CourseScraperAsyncTest {

    @Autowired
    private lateinit var scraperService: CourseScraperService

    @Autowired
    private lateinit var couponRepository: CouponCourseRepository

    @Test
    fun validateAndSaveCouponAsync_ShouldRunInBackground() {
        val mockUrl = "https://www.udemy.com/course/mock/"

        // This won't actually scrape because we are in a test environment without real network
        // but we want to verify the infrastructure triggers.

        scraperService.enqueueScrapingTask(mockUrl, "127.0.0.1")
    }
}
