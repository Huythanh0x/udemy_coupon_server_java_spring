package com.thanh0x.coursedeal.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing a WebAuthn (Passkey) credential.
 */
@Entity
@Table(name = "user_passkey_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 1024)
    private byte[] credentialId;

    @Column(nullable = false, length = 2048)
    private byte[] publicKey;

    private Long signatureCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private UserEntity user;
}
