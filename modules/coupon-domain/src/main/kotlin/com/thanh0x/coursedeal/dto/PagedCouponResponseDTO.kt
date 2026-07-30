package com.thanh0x.coursedeal.dto

/**
 * Data class representing a response containing a paginated list of coupon data.
 */
data class PagedCouponResponseDTO(
    // Epoch milliseconds
    val lastFetchTime: Long? = null,
    val totalCoupon: Long? = null,
    val totalPage: Int? = null,
    val currentPage: Int? = null,
    val courses: List<CouponSummaryDTO>? = null,
)
