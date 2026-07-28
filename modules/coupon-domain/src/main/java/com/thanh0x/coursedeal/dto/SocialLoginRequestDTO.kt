package com.thanh0x.coursedeal.dto

import com.thanh0x.coursedeal.model.user.AuthProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Request DTO for social login.
 */
data class SocialLoginRequestDTO(
    @field:NotNull(message = "Provider is required")
    val provider: AuthProvider? = null,

    @field:NotBlank(message = "ID Token is required")
    val idToken: String = "",

    val fcmToken: String? = null
)
