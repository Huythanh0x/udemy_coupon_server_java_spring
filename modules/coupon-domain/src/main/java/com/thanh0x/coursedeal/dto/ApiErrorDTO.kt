package com.thanh0x.coursedeal.dto

import java.util.Date

/**
 * Standardized error response for the API.
 */
data class ApiErrorDTO(
    val timestamp: Date? = null,
    val status: Int = 0,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)
