package com.thanh0x.coursedeal.dto

import com.thanh0x.coursedeal.model.coupon.CourseLevel
import java.time.Instant
import java.time.LocalDateTime

/**
 * Full DTO for coupon detail view.
 */
data class CouponDetailDTO(
    val courseId: Int,
    val category: String? = null,
    val subCategory: String? = null,
    val title: String? = null,
    val contentLength: Int = 0,
    val level: CourseLevel? = null,
    val author: String? = null,
    val rating: Float = 0f,
    val reviews: Int = 0,
    val students: Int = 0,
    val couponCode: String? = null,
    val previewImage: String? = null,
    val couponUrl: String? = null,
    val expiredDate: Instant? = null,
    val usesRemaining: Int = 0,
    val heading: String? = null,
    val description: String? = null,
    val previewVideo: String? = null,
    val language: String? = null,
    @get:JvmName("isNew")
    val isNew: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
