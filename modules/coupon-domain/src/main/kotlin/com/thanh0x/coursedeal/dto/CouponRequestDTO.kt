package com.thanh0x.coursedeal.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request body for creating a new coupon.
 */
data class CouponRequestDTO(
    /**
     * The Udemy coupon URL.
     */
    @field:NotBlank(message = "Coupon URL is required")
    val couponUrl: String = ""
)
