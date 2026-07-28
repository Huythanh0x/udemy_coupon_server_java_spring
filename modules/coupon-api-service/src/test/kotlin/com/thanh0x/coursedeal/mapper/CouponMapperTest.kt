package com.thanh0x.coursedeal.mapper

import com.thanh0x.coursedeal.model.coupon.CouponCourseData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class CouponMapperTest {
    private val mapper = CouponMapper()

    @Test
    fun toSummaryDto_ShouldMapCorrectly() {
        val entity =
            CouponCourseData(
                courseId = 123,
                title = "Test Course",
                category = "Development",
                author = "Author Name",
                rating = 4.5f,
                students = 1000,
                previewImage = "image.jpg",
                couponUrl = "http://udemy.com/test",
                expiredDate = Instant.now(),
                usesRemaining = 50,
                isNew = true,
                description = "Long description that should be hidden",
            )

        val dto = mapper.toSummaryDto(entity)

        assertEquals(entity.courseId, dto.courseId)
        assertEquals(entity.title, dto.title)
        assertEquals(entity.category, dto.category)
        assertEquals(entity.rating, dto.rating)
        assertEquals(entity.isNew, dto.isNew)
    }

    @Test
    fun toDetailDto_ShouldIncludeAllFields() {
        val entity =
            CouponCourseData(
                courseId = 123,
                title = "Test Course",
                description = "Detailed description",
                previewVideo = "video.mp4",
            )

        val dto = mapper.toDetailDto(entity)

        assertEquals(entity.courseId, dto.courseId)
        assertEquals(entity.description, dto.description)
        assertEquals(entity.previewVideo, dto.previewVideo)
    }
}
