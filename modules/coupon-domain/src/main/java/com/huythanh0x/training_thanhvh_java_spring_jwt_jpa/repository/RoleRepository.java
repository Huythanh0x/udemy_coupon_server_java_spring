package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository;


import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}