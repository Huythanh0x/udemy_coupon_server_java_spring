package com.thanh0x.coursedeal.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * Request body for creating a new coupon.
 */
data class CouponRequestDTO(
    /**
     * The Udemy coupon URL.
     */
    @field:NotBlank(message = "Coupon URL is required")
    @field:Pattern(
        regexp = "^https?://.*udemy\\.com/.*",
        message = "Invalid Udemy coupon URL",
    )
    val couponUrl: String = "",
)
