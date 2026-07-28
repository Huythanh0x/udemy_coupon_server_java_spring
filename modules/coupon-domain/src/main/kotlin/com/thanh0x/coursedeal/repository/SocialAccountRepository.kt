package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.user.AuthProvider
import com.thanh0x.coursedeal.model.user.SocialAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SocialAccountRepository : JpaRepository<SocialAccount, Long> {
    fun findByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): SocialAccount?
}
