package com.thanh0x.coursedeal.dto;

import com.thanh0x.coursedeal.model.user.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for social login.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginRequestDTO {
    @NotNull(message = "Provider is required")
    private AuthProvider provider;

    @NotBlank(message = "ID Token is required")
    private String idToken;

    private String fcmToken;
}
