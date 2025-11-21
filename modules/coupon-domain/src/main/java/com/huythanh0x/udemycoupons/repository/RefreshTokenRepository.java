package com.huythanh0x.udemycoupons.repository;

import com.huythanh0x.udemycoupons.model.user.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {
    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);
}