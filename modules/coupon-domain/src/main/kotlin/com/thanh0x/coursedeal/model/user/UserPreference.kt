package com.thanh0x.coursedeal.model.user

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entity representing user preferences for notifications and filtering.
 */
@Entity
@Table(name = "user_preferences")
class UserPreference(
    @Id
    var userId: Int? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null,
    @ElementCollection
    @CollectionTable(name = "user_preference_categories", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "category")
    var categories: MutableSet<String> = mutableSetOf(),
    @ElementCollection
    @CollectionTable(name = "user_preference_keywords", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "keyword")
    var keywords: MutableSet<String> = mutableSetOf(),
    var notificationsEnabled: Boolean = false,
    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null,
)
