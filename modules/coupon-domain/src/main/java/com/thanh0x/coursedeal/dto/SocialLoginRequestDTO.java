package com.thanh0x.coursedeal.dto;

import com.thanh0x.coursedeal.model.user.AuthProvider;
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
    private AuthProvider provider;
    private String idToken;
    private String fcmToken;
}
