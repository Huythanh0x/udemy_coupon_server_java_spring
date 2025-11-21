package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {
    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);
}