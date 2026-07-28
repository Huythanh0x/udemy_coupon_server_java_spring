package com.thanh0x.coursedeal.model.user

import jakarta.persistence.*

/**
 * Entity representing a linked social account (Google, Apple).
 */
@Entity
@Table(
    name = "user_social_accounts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "providerId"])],
)
class SocialAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    var provider: AuthProvider? = null,
    var providerId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null,
)
