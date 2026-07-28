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
    val createdFormatted: String? = null, // e.g., "4 days ago"
    val user: ReviewUserDTO? = null,
    val response: ReviewResponseDTO? = null // Instructor response, if any
)
