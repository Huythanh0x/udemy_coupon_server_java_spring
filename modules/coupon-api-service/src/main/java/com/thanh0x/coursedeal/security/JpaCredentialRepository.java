package com.thanh0x.coursedeal.security;

import com.thanh0x.coursedeal.model.user.PasskeyCredential;
import com.thanh0x.coursedeal.model.user.UserEntity;
import com.thanh0x.coursedeal.repository.PasskeyCredentialRepository;
import com.thanh0x.coursedeal.repository.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bridges the Yubico WebAuthn library with our JPA database.
 */
@Component
public class JpaCredentialRepository implements CredentialRepository {

    private final PasskeyCredentialRepository passkeyRepository;
    private final UserRepository userRepository;

    public JpaCredentialRepository(PasskeyCredentialRepository passkeyRepository, UserRepository userRepository) {
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> passkeyRepository.findAllByUser(user).stream()
                        .map(cred -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(cred.getCredentialId()))
                                .build())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new ByteArray(String.valueOf(user.getId()).getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        int userId = Integer.parseInt(new String(userHandle.getBytes()));
        return userRepository.findById(userId).map(UserEntity::getUsername);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return passkeyRepository.findByCredentialId(credentialId.getBytes())
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(new ByteArray(cred.getCredentialId()))
                        .userHandle(new ByteArray(String.valueOf(cred.getUser().getId()).getBytes()))
                        .publicKeyCose(new ByteArray(cred.getPublicKey()))
                        .signatureCount(cred.getSignatureCount())
                        .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return passkeyRepository.findByCredentialId(credentialId.getBytes()).stream()
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(new ByteArray(cred.getCredentialId()))
                        .userHandle(new ByteArray(String.valueOf(cred.getUser().getId()).getBytes()))
                        .publicKeyCose(new ByteArray(cred.getPublicKey()))
                        .signatureCount(cred.getSignatureCount())
                        .build())
                .collect(Collectors.toSet());
    }
}
