package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.user.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Int> {
    fun findByName(name: String): Role?
}
