package com.thanh0x.coursedeal.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.thanh0x.coursedeal.config.IdentityProperties
import com.thanh0x.coursedeal.dto.AuthResponseDTO
import com.thanh0x.coursedeal.dto.SocialLoginRequestDTO
import com.thanh0x.coursedeal.exception.BadRequestException
import com.thanh0x.coursedeal.model.user.AuthProvider
import com.thanh0x.coursedeal.model.user.SocialAccount
import com.thanh0x.coursedeal.model.user.UserEntity
import com.thanh0x.coursedeal.repository.SocialAccountRepository
import com.thanh0x.coursedeal.repository.UserRepository
import com.thanh0x.coursedeal.security.TokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections

@Service
class SocialAuthService(
    private val userRepository: UserRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val tokenProvider: TokenProvider,
    private val properties: IdentityProperties,
) {
    @Transactional
    fun login(request: SocialLoginRequestDTO): AuthResponseDTO {
        val user: UserEntity =
            when (request.provider) {
                AuthProvider.GOOGLE -> verifyGoogleToken(request.idToken!!)
                AuthProvider.APPLE -> verifyAppleToken(request.idToken!!)
                else -> throw BadRequestException("Unsupported social provider")
            }

        if (request.fcmToken != null) {
            user.fcmToken = request.fcmToken
            userRepository.save(user)
        }

        val token = tokenProvider.createToken(user)
        return AuthResponseDTO(
            accessToken = token,
            tokenType = "Bearer",
        )
    }

    private fun verifyGoogleToken(idTokenString: String): UserEntity {
        try {
            val verifier =
                GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
                    .setAudience(Collections.singletonList(properties.googleClientId))
                    .build()

            val idToken = verifier.verify(idTokenString) ?: throw BadRequestException("Invalid Google ID Token")

            val payload = idToken.payload
            val email = payload.email
            val providerId = payload.subject
            val name = payload["name"] as String?

            return findOrCreateUser(email, name, AuthProvider.GOOGLE, providerId)
        } catch (e: Exception) {
            throw BadRequestException("Failed to verify Google Token: " + e.message)
        }
    }

    private fun verifyAppleToken(idTokenString: String): UserEntity {
        // Mocking Apple for now - requires JWT validation with Apple's public keys
        throw UnsupportedOperationException("Apple login not yet implemented")
    }

    private fun findOrCreateUser(
        email: String,
        name: String?,
        provider: AuthProvider,
        providerId: String,
    ): UserEntity {
        val socialAccount = socialAccountRepository.findByProviderAndProviderId(provider, providerId)
        if (socialAccount != null) {
            return socialAccount.user!!
        }

        val user =
            userRepository.findByEmail(email) ?: run {
                val newUser =
                    UserEntity(
                        email = email,
                        fullName = name,
                        // fallback username
                        username = email,
                    )
                userRepository.save(newUser)
            }

        val newSocialAccount =
            SocialAccount(
                provider = provider,
                providerId = providerId,
                user = user,
            )
        socialAccountRepository.save(newSocialAccount)

        return user
    }
}
