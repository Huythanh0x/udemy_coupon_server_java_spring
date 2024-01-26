package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.RefreshTokenEntity;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.RefreshTokenRepository;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RefreshTokenService {
    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshTokenEntity saveRefreshToken(String username) {
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder().
                refreshToken(getRefreshTokenString())
                .user(userRepository.findByUsername(username).get())
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public String getRefreshTokenString() {
        return UUID.randomUUID().toString();
    }
}