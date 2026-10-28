package com.dunkweb.talentbridge.infrastructure.persistence.entity;

import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfileJpaEntity {

    @Id
    private UUID id;

    @Column(name = "professional_title", nullable = false, length = 160)
    private String professionalTitle;

    @Column(name = "professional_summary", nullable = false, length = 1000)
    private String professionalSummary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_profile_objectives", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "objective", nullable = false, length = 80)
    @Builder.Default
    private Set<CareerObjective> objectives = new LinkedHashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<SkillPreferenceJpaEntity> skills = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
