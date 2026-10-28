package com.dunkweb.talentbridge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "skill_preferences", indexes = {
        @Index(name = "idx_skill_preferences_skill", columnList = "skill"),
        @Index(name = "idx_skill_preferences_weight", columnList = "weight")
})
public class SkillPreferenceJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfileJpaEntity userProfile;

    @Column(nullable = false, length = 120)
    private String skill;

    @Column(nullable = false)
    private Integer weight;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
