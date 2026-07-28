package com.thanh0x.coursedeal.dto

import com.thanh0x.coursedeal.model.coupon.CourseLevel
import java.time.Instant

/**
 * Optimized DTO for coupon list view.
 */
data class CouponSummaryDTO(
    val courseId: Int,
    val title: String? = null,
    val category: String? = null,
    val level: CourseLevel? = null,
    val author: String? = null,
    val rating: Float = 0f,
    val students: Int = 0,
    val previewImage: String? = null,
    val couponUrl: String? = null,
    val expiredDate: Instant? = null,
    val usesRemaining: Int = 0,
    @get:JvmName("isNew")
    val isNew: Boolean = false
)
