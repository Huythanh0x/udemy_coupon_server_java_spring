package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    @Nullable
    private String accessToken;
    private String refreshToken;
}