package com.thanh0x.coursedeal.repository;

import com.thanh0x.coursedeal.model.user.PasskeyCredential;
import com.thanh0x.coursedeal.model.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {
    Optional<PasskeyCredential> findByCredentialId(byte[] credentialId);
    List<PasskeyCredential> findAllByUser(UserEntity user);
}
