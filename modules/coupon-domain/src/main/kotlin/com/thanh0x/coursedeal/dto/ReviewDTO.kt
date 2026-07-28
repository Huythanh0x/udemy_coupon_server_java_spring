package com.thanh0x.coursedeal.dto

/**
 * DTO for individual course review.
 */
data class ReviewDTO(
    val id: Long? = null,
    val content: String? = null,
    val contentHtml: String? = null,
    val rating: Float? = null,
    val created: String? = null,
    // e.g., "4 days ago"
    val createdFormatted: String? = null,
    val user: ReviewUserDTO? = null,
    // Instructor response, if any
    val response: ReviewResponseDTO? = null,
)
