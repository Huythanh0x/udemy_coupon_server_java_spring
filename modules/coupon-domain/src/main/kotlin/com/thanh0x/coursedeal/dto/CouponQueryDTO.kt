package com.thanh0x.coursedeal.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data class to encapsulate coupon listing and filtering parameters.
 * Used to avoid long parameter lists in controllers and services.
 */
@Schema(description = "Query parameters for searching and filtering coupons")
data class CouponQueryDTO(
    @Schema(description = "Filter by course category (exact, case-insensitive)", example = "Development")
    val category: String = "",

    @Schema(description = "Minimum rating filter. Use -1 to disable.", example = "4.5")
    val rating: String = "-1",

    @Schema(description = "Minimum content length (minutes) filter. Use -1 to disable.", example = "60")
    val contentLength: String = "-1",

    @Schema(description = "Filter by course difficulty level", example = "BEGINNER")
    val level: String = "",

    @Schema(description = "Filter by course language", example = "English")
    val language: String = "",

    @Schema(description = "Free-text search across title, description, and heading", example = "Kotlin")
    val query: String = "",

    @Schema(
        description = "Field to sort by",
        allowableValues = ["students", "rating", "reviews", "expiredTime", "createdAt"],
        defaultValue = "createdAt"
    )
    val sortBy: String = "createdAt",

    @Schema(description = "Sort direction", allowableValues = ["asc", "desc"], defaultValue = "desc")
    val sortOrder: String = "desc",

    @Schema(description = "Zero-indexed page number", defaultValue = "0")
    val pageIndex: String = "0",

    @Schema(description = "Number of items per page (max 1000)", defaultValue = "1000")
    val numberPerPage: String = "1000",
)
