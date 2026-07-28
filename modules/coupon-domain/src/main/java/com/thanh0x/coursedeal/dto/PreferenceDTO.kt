package com.thanh0x.coursedeal.dto

import jakarta.validation.constraints.NotNull

/**
 * DTO for user preferences.
 */
data class PreferenceDTO(
    @field:NotNull(message = "Categories set cannot be null")
    val categories: Set<String> = emptySet(),

    @field:NotNull(message = "Keywords set cannot be null")
    val keywords: Set<String> = emptySet(),

    val notificationsEnabled: Boolean = false
)
