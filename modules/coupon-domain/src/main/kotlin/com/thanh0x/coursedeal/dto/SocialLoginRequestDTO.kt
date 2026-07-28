package com.thanh0x.coursedeal.dto

import com.thanh0x.coursedeal.model.user.AuthProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO for social login.
 */
data class SocialLoginRequestDTO(
    @field:NotNull(message = "Provider is required")
    val provider: AuthProvider? = null,
    @field:NotBlank(message = "ID Token is required")
    @field:Size(min = 10, message = "ID Token is too short")
    val idToken: String = "",
    @field:Size(max = 1000)
    val fcmToken: String? = null,
)
