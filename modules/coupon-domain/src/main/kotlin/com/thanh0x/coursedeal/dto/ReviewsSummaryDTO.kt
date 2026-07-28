package com.thanh0x.coursedeal.dto

/**
 * DTO for reviews summary information.
 */
data class ReviewsSummaryDTO(
    val totalCount: Int? = null,
    val averageRating: Float? = null,
    // First 3-5 reviews
    val recentReviews: List<ReviewDTO>? = null,
)
