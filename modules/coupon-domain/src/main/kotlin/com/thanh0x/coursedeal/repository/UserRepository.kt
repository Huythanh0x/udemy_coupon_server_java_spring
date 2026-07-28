package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findByUsername(username: String): UserEntity?

    fun existsByUsername(username: String): Boolean

    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean
}
