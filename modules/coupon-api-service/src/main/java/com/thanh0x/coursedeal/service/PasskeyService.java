package com.thanh0x.coursedeal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh0x.coursedeal.dto.AuthResponseDTO;
import com.thanh0x.coursedeal.exception.BadRequestException;
import com.thanh0x.coursedeal.model.user.PasskeyCredential;
import com.thanh0x.coursedeal.model.user.UserEntity;
import com.thanh0x.coursedeal.repository.PasskeyCredentialRepository;
import com.thanh0x.coursedeal.repository.UserRepository;
import com.thanh0x.coursedeal.security.TokenProvider;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Service for handling WebAuthn (Passkey) registration and authentication.
 */
@Service
public class PasskeyService {
    private static final Logger log = LoggerFactory.getLogger(PasskeyService.class);

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final PasskeyCredentialRepository passkeyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REG_OPTIONS_PREFIX = "webauthn:reg:";
    private static final String AUTH_REQUEST_PREFIX = "webauthn:auth:";

    public PasskeyService(RelyingParty relyingParty,
                          UserRepository userRepository,
                          PasskeyCredentialRepository passkeyRepository,
                          RedisTemplate<String, Object> redisTemplate,
                          TokenProvider tokenProvider) {
        this.relyingParty = relyingParty;
        this.userRepository = userRepository;
        this.passkeyRepository = passkeyRepository;
        this.redisTemplate = redisTemplate;
        this.tokenProvider = tokenProvider;
    }

    public PublicKeyCredentialCreationOptions startRegistration(String email, String fullName) {
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> 
                userRepository.save(UserEntity.builder()
                        .email(email)
                        .username(email)
                        .fullName(fullName)
                        .build())
        );

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(UserIdentity.builder()
                                .name(user.getEmail())
                                .displayName(user.getFullName())
                                .id(new ByteArray(String.valueOf(user.getId()).getBytes()))
                                .build())
                        .build()
        );

        storeInRedis(REG_OPTIONS_PREFIX + email, options);
        return options;
    }

    @Transactional
    public AuthResponseDTO finishRegistration(String email, String responseJson) {
        try {
            PublicKeyCredentialCreationOptions options = getFromRedis(REG_OPTIONS_PREFIX + email, PublicKeyCredentialCreationOptions.class);
            
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc = 
                    PublicKeyCredential.parseRegistrationResponseJson(responseJson);

            RegistrationResult result = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(options)
                            .response(pkc)
                            .build()
            );

            UserEntity user = userRepository.findByEmail(email).orElseThrow();
            
            PasskeyCredential credential = PasskeyCredential.builder()
                    .credentialId(result.getKeyId().getId().getBytes())
                    .publicKey(result.getPublicKeyCose().getBytes())
                    .signatureCount(result.getSignatureCount())
                    .user(user)
                    .build();
            
            passkeyRepository.save(credential);
            
            return AuthResponseDTO.builder()
                    .accessToken(tokenProvider.createToken(user))
                    .tokenType("Bearer")
                    .build();

        } catch (Exception e) {
            log.error("Registration failed for {}: {}", email, e.getMessage());
            throw new BadRequestException("Registration failed: " + e.getMessage());
        }
    }

    public AssertionRequest startAuthentication(String email) {
        AssertionRequest request = relyingParty.startAssertion(
                StartAssertionOptions.builder()
                        .username(java.util.Optional.of(email))
                        .build()
        );

        storeInRedis(AUTH_REQUEST_PREFIX + email, request);
        return request;
    }

    @Transactional
    public AuthResponseDTO finishAuthentication(String email, String responseJson) {
        try {
            AssertionRequest request = getFromRedis(AUTH_REQUEST_PREFIX + email, AssertionRequest.class);
            
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc = 
                    PublicKeyCredential.parseAssertionResponseJson(responseJson);

            AssertionResult result = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                            .request(request)
                            .response(pkc)
                            .build()
            );

            if (result.isSuccess()) {
                UserEntity user = userRepository.findByEmail(email).orElseThrow();
                
                passkeyRepository.findByCredentialId(result.getCredentialId().getBytes())
                        .ifPresent(cred -> {
                            cred.setSignatureCount(result.getSignatureCount());
                            passkeyRepository.save(cred);
                        });

                return AuthResponseDTO.builder()
                        .accessToken(tokenProvider.createToken(user))
                        .tokenType("Bearer")
                        .build();
            }
            throw new BadRequestException("Authentication failed");

        } catch (Exception e) {
            log.error("Authentication failed for {}: {}", email, e.getMessage());
            throw new BadRequestException("Authentication failed: " + e.getMessage());
        }
    }

    private void storeInRedis(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(5));
        } catch (Exception e) {
            throw new RuntimeException("Failed to store WebAuthn state", e);
        }
    }

    private <T> T getFromRedis(String key, Class<T> clazz) {
        try {
            String json = (String) redisTemplate.opsForValue().get(key);
            if (json == null) throw new BadRequestException("Handshake expired or not found");
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new BadRequestException("Failed to retrieve WebAuthn state: " + e.getMessage());
        }
    }
}
