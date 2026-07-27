package com.thanh0x.coursedeal.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.thanh0x.coursedeal.dto.AuthResponseDTO;
import com.thanh0x.coursedeal.dto.SocialLoginRequestDTO;
import com.thanh0x.coursedeal.exception.BadRequestException;
import com.thanh0x.coursedeal.model.user.AuthProvider;
import com.thanh0x.coursedeal.model.user.SocialAccount;
import com.thanh0x.coursedeal.model.user.UserEntity;
import com.thanh0x.coursedeal.repository.SocialAccountRepository;
import com.thanh0x.coursedeal.repository.UserRepository;
import com.thanh0x.coursedeal.security.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class SocialAuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TokenProvider tokenProvider;
    private final String googleClientId;

    public SocialAuthService(UserRepository userRepository,
                             SocialAccountRepository socialAccountRepository,
                             TokenProvider tokenProvider,
                             @Value("${custom.google-client-id:}") String googleClientId) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.tokenProvider = tokenProvider;
        this.googleClientId = googleClientId;
    }

    @Transactional
    public AuthResponseDTO login(SocialLoginRequestDTO request) {
        UserEntity user;
        if (request.getProvider() == AuthProvider.GOOGLE) {
            user = verifyGoogleToken(request.getIdToken());
        } else if (request.getProvider() == AuthProvider.APPLE) {
            user = verifyAppleToken(request.getIdToken());
        } else {
            throw new BadRequestException("Unsupported social provider");
        }

        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);
        }

        String token = tokenProvider.createToken(user);
        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    private UserEntity verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BadRequestException("Invalid Google ID Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String providerId = payload.getSubject();
            String name = (String) payload.get("name");

            return findOrCreateUser(email, name, AuthProvider.GOOGLE, providerId);
        } catch (Exception e) {
            throw new BadRequestException("Failed to verify Google Token: " + e.getMessage());
        }
    }

    private UserEntity verifyAppleToken(String idTokenString) {
        // Mocking Apple for now - requires JWT validation with Apple's public keys
        // In a real implementation, you would use a library like Nimbus JOSE + JWT
        throw new UnsupportedOperationException("Apple login not yet implemented");
    }

    private UserEntity findOrCreateUser(String email, String name, AuthProvider provider, String providerId) {
        Optional<SocialAccount> socialAccount = socialAccountRepository.findByProviderAndProviderId(provider, providerId);
        if (socialAccount.isPresent()) {
            return socialAccount.get().getUser();
        }

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .fullName(name)
                    .username(email) // fallback username
                    .build();
            return userRepository.save(newUser);
        });

        SocialAccount newSocialAccount = SocialAccount.builder()
                .provider(provider)
                .providerId(providerId)
                .user(user)
                .build();
        socialAccountRepository.save(newSocialAccount);
        
        return user;
    }
}
