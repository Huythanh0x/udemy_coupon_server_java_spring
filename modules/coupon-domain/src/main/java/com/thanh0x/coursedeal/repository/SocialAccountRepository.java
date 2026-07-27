package com.thanh0x.coursedeal.repository;

import com.thanh0x.coursedeal.model.user.AuthProvider;
import com.thanh0x.coursedeal.model.user.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
