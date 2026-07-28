package com.thanh0x.coursedeal.security

import com.thanh0x.coursedeal.repository.PasskeyCredentialRepository
import com.thanh0x.coursedeal.repository.UserRepository
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * Bridges the Yubico WebAuthn library with our JPA database.
 */
@Component
class JpaCredentialRepository(
    private val passkeyRepository: PasskeyCredentialRepository,
    private val userRepository: UserRepository,
) : CredentialRepository {
    override fun getCredentialIdsForUsername(username: String): Set<PublicKeyCredentialDescriptor> {
        val user = userRepository.findByUsername(username)
        return user?.let { u ->
            passkeyRepository.findAllByUser(u)
                .map { cred ->
                    PublicKeyCredentialDescriptor.builder()
                        .id(ByteArray(cred.credentialId!!))
                        .build()
                }
                .toSet()
        } ?: emptySet()
    }

    override fun getUserHandleForUsername(username: String): Optional<ByteArray> {
        val user = userRepository.findByUsername(username)
        return Optional.ofNullable(user).map { u -> ByteArray(u.id.toString().toByteArray()) }
    }

    override fun getUsernameForUserHandle(userHandle: ByteArray): Optional<String> {
        val userId = String(userHandle.bytes).toInt()
        return userRepository.findById(userId).map { it.username }
    }

    override fun lookup(
        credentialId: ByteArray,
        userHandle: ByteArray,
    ): Optional<RegisteredCredential> {
        val cred = passkeyRepository.findByCredentialId(credentialId.bytes)
        return Optional.ofNullable(cred).map { c ->
            RegisteredCredential.builder()
                .credentialId(ByteArray(c.credentialId!!))
                .userHandle(ByteArray(c.user?.id.toString().toByteArray()))
                .publicKeyCose(ByteArray(c.publicKey!!))
                .signatureCount(c.signatureCount ?: 0L)
                .build()
        }
    }

    override fun lookupAll(credentialId: ByteArray): Set<RegisteredCredential> {
        val cred = passkeyRepository.findByCredentialId(credentialId.bytes)
        return cred?.let { c ->
            setOf(
                RegisteredCredential.builder()
                    .credentialId(ByteArray(c.credentialId!!))
                    .userHandle(ByteArray(c.user?.id.toString().toByteArray()))
                    .publicKeyCose(ByteArray(c.publicKey!!))
                    .signatureCount(c.signatureCount ?: 0L)
                    .build(),
            )
        } ?: emptySet()
    }
}
