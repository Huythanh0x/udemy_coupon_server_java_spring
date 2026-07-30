package com.thanh0x.coursedeal.dto

/**
 * DTO for instructor response to a review.
 */
data class ReviewResponseDTO(
    val content: String? = null,
    val contentHtml: String? = null,
    val created: String? = null,
    // e.g., "18 hours ago"
    val createdFormatted: String? = null,
    val user: ReviewUserDTO? = null,
)
