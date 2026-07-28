package com.thanh0x.coursedeal.model.user

import jakarta.persistence.*

/**
 * Entity representing a WebAuthn (Passkey) credential.
 */
@Entity
@Table(name = "user_passkey_credentials")
class PasskeyCredential(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false, length = 1024)
    var credentialId: ByteArray? = null,

    @Column(nullable = false, length = 2048)
    var publicKey: ByteArray? = null,

    var signatureCount: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null
)
