package com.thanh0x.coursedeal.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing user preferences for notifications and filtering.
 */
@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private UserEntity user;

    @ElementCollection
    @CollectionTable(name = "user_preference_categories", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "category")
    @Builder.Default
    private Set<String> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_preference_keywords", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "keyword")
    @Builder.Default
    private Set<String> keywords = new HashSet<>();

    private boolean notificationsEnabled;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
