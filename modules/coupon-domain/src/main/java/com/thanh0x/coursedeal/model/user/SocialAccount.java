package com.thanh0x.coursedeal.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing a linked social account (Google, Apple).
 */
@Entity
@Table(name = "user_social_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "providerId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private UserEntity user;
}
