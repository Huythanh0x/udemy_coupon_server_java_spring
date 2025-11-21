package com.huythanh0x.udemycoupons.repository;


import com.huythanh0x.udemycoupons.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}