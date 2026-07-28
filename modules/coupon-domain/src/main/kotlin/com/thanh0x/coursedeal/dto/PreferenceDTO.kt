package com.thanh0x.coursedeal.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * DTO for user preferences.
 */
data class PreferenceDTO(
    @field:NotNull(message = "Categories set cannot be null")
    @field:Size(max = 20, message = "Too many categories")
    val categories: Set<String> = emptySet(),

    @field:NotNull(message = "Keywords set cannot be null")
    @field:Size(max = 20, message = "Too many keywords")
    val keywords: Set<String> = emptySet(),

    val notificationsEnabled: Boolean = false
)
