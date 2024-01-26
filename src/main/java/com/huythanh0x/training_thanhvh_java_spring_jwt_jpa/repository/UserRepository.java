package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository;


import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);

    Boolean existsByUsername(String username);
}