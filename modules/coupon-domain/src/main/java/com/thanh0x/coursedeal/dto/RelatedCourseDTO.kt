package com.thanh0x.coursedeal.dto

/**
 * DTO for related/recommended course.
 */
data class RelatedCourseDTO(
    val courseId: Int? = null,
    val title: String? = null,
    val headline: String? = null,
    val url: String? = null,
    val image240x135: String? = null,
    val image480x270: String? = null,
    val image750x422: String? = null,
    val author: String? = null,
    val rating: Float? = null,
    val numReviews: Int? = null,
    val numSubscribers: Int? = null,
    val contentInfo: String? = null, // e.g., "2.5 hours"
    val instructionalLevel: String? = null
)
