package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.user.PasskeyCredential
import com.thanh0x.coursedeal.model.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PasskeyCredentialRepository : JpaRepository<PasskeyCredential, Long> {
    fun findByCredentialId(credentialId: ByteArray): PasskeyCredential?
    fun findAllByUser(user: UserEntity): List<PasskeyCredential>
}
