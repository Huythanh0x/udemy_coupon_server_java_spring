package com.thanh0x.coursedeal.config

import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.RelyingPartyIdentity
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for WebAuthn (Passkey) support.
 */
@Configuration
class WebAuthnConfig {

    @Value("\${custom.webauthn.rp-id:coursedeal.thanh0x.com}")
    private lateinit var rpId: String

    @Value("\${custom.webauthn.rp-name:Course Deal}")
    private lateinit var rpName: String

    @Value("\${custom.webauthn.origins:https://coursedeal.thanh0x.com}")
    private lateinit var origins: String

    @Bean
    fun relyingParty(credentialRepository: CredentialRepository): RelyingParty {
        val rpIdentity = RelyingPartyIdentity.builder()
            .id(rpId)
            .name(rpName)
            .build()

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(credentialRepository)
            .origins(origins.split(",").toSet())
            .build()
    }
}
