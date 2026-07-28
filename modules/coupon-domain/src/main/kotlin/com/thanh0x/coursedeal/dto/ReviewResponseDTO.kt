package com.thanh0x.coursedeal.dto

/**
 * DTO for instructor response to a review.
 */
data class ReviewResponseDTO(
    val content: String? = null,
    val contentHtml: String? = null,
    val created: String? = null,
    val createdFormatted: String? = null, // e.g., "18 hours ago"
    val user: ReviewUserDTO? = null
)
