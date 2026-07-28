package com.thanh0x.coursedeal.dto

/**
 * Data class to encapsulate coupon listing and filtering parameters.
 * Used to avoid long parameter lists in controllers and services.
 */
data class CouponQueryDTO(
    val category: String = "",
    val rating: String = "-1",
    val contentLength: String = "-1",
    val level: String = "",
    val language: String = "",
    val query: String = "",
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc",
    val pageIndex: String = "0",
    val numberPerPage: String = "10",
)
