package com.thanh0x.coursedeal.dto

import com.thanh0x.coursedeal.model.coupon.CourseLevel
import java.time.Instant

/**
 * DTO for comprehensive course details including reviews summary, curriculum, and related courses.
 */
data class CourseDetailDTO(
    // Basic course information (from existing CouponCourseData)
    val courseId: Int? = null,
    val title: String? = null,
    val heading: String? = null,
    val description: String? = null,
    val author: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val level: CourseLevel? = null,
    val language: String? = null,
    val rating: Float? = null,
    val reviews: Int? = null,
    val students: Int? = null,
    val contentLength: Int? = null,
    val previewImage: String? = null,
    val previewVideo: String? = null,
    val couponUrl: String? = null,
    val couponCode: String? = null,
    val usesRemaining: Int? = null,
    val expiredDate: Instant? = null,
    // Additional details from Udemy API
    val reviewsSummary: ReviewsSummaryDTO? = null,
    val curriculum: CurriculumDTO? = null,
    val relatedCourses: List<RelatedCourseDTO>? = null,
    val pricingInfo: PricingInfoDTO? = null,
    val incentives: IncentivesDTO? = null,
    // List of preview videos from preview page
    val previewVideos: List<PreviewVideoDTO>? = null,
)
