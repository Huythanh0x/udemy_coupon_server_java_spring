package com.thanh0x.coursedeal.dto

/**
 * DTO for paginated course reviews response.
 */
data class CourseReviewsDTO(
    val reviews: List<ReviewDTO>? = null,
    val totalCount: Int? = null,
    val currentPage: Int? = null,
    val hasNext: Boolean? = null,
    val hasPrevious: Boolean? = null,
    val nextUrl: String? = null,
    val previousUrl: String? = null,
)
