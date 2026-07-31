package com.thanh0x.coursedeal.dto

import com.thanh0x.coursedeal.model.coupon.CourseLevel

/**
 * Optimized DTO for coupon list view.
 */
data class CouponSummaryDTO(
    val courseId: Int,
    val title: String? = null,
    val category: String? = null,
    val level: CourseLevel? = null,
    val language: String? = null,
    val author: String? = null,
    val rating: Float = 0f,
    val reviews: Int = 0,
    val students: Int = 0,
    val previewImage: String? = null,
    val expiredTime: Long? = null,
    val createdAt: Long? = null,
    @get:JvmName("isNew")
    val isNew: Boolean = false,
)
