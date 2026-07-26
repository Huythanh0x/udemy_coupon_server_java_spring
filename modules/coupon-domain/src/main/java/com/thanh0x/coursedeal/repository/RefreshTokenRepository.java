package com.thanh0x.coursedeal.repository;

import com.thanh0x.coursedeal.model.user.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {
    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);
}