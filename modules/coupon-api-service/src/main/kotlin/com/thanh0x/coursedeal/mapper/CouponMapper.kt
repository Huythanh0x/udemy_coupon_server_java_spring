package com.thanh0x.coursedeal.mapper

import com.thanh0x.coursedeal.dto.CouponDetailDTO
import com.thanh0x.coursedeal.dto.CouponSummaryDTO
import com.thanh0x.coursedeal.model.coupon.CouponCourseData
import org.springframework.stereotype.Component

@Component
class CouponMapper {
    fun toSummaryDto(entity: CouponCourseData): CouponSummaryDTO {
        return CouponSummaryDTO(
            courseId = entity.courseId,
            title = entity.title,
            category = entity.category,
            level = entity.level,
            language = entity.language,
            author = entity.author,
            rating = entity.rating,
            reviews = entity.reviews,
            students = entity.students,
            previewImage = entity.previewImage,
            expiredTime = entity.expiredDate,
            newest = entity.createdAt,
            isNew = entity.isNew,
        )
    }

    fun toDetailDto(entity: CouponCourseData): CouponDetailDTO {
        return CouponDetailDTO(
            courseId = entity.courseId,
            category = entity.category,
            subCategory = entity.subCategory,
            title = entity.title,
            contentLength = entity.contentLength,
            level = entity.level,
            author = entity.author,
            rating = entity.rating,
            reviews = entity.reviews,
            students = entity.students,
            couponCode = entity.couponCode,
            previewImage = entity.previewImage,
            couponUrl = entity.couponUrl,
            expiredDate = entity.expiredDate,
            usesRemaining = entity.usesRemaining,
            heading = entity.heading,
            description = entity.description,
            previewVideo = entity.previewVideo,
            language = entity.language,
            isNew = entity.isNew,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
