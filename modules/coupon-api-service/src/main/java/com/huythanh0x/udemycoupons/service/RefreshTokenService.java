package com.huythanh0x.udemycoupons.service;

import com.huythanh0x.udemycoupons.model.user.RefreshTokenEntity;
import com.huythanh0x.udemycoupons.repository.RefreshTokenRepository;
import com.huythanh0x.udemycoupons.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service class for handling refresh tokens.
 */
@Service
public class RefreshTokenService {
    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Saves a new refresh token for the specified user.
     *
     * @param username The username of the user to save the refresh token for
     * @return The saved refresh token entity
     */
    public RefreshTokenEntity saveRefreshToken(String username) {
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder().
                refreshToken(getRefreshTokenString())
                .user(userRepository.findByUsername(username).get())
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    /**
     * Generates a new random refresh token string using a UUID.
     *
     * @return a string representing the new refresh token
     */
    public String getRefreshTokenString() {
        return UUID.randomUUID().toString();
    }
}