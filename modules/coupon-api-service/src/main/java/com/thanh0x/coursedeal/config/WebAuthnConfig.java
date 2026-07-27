package com.thanh0x.coursedeal.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.yubico.webauthn.CredentialRepository;

import java.util.Collections;
import java.util.Set;

/**
 * Configuration for WebAuthn (Passkey) support.
 */
@Configuration
public class WebAuthnConfig {

    @Value("${custom.webauthn.rp-id:coursedeal.thanh0x.com}")
    private String rpId;

    @Value("${custom.webauthn.rp-name:Course Deal}")
    private String rpName;

    @Value("${custom.webauthn.origins:https://coursedeal.thanh0x.com}")
    private String origins;

    @Bean
    public RelyingParty relyingParty(CredentialRepository credentialRepository) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(rpId)
                .name(rpName)
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(Set.of(origins.split(",")))
                .build();
    }
}
