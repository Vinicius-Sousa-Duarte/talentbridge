package com.dunkweb.talentbridge.infrastructure.persistence.mapper;

import com.dunkweb.talentbridge.domain.entity.SkillPreference;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.infrastructure.persistence.entity.SkillPreferenceJpaEntity;
import com.dunkweb.talentbridge.infrastructure.persistence.entity.UserProfileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class UserProfilePersistenceMapper {

    public UserProfileJpaEntity toEntity(UserProfile profile) {
        UserProfileJpaEntity entity = UserProfileJpaEntity.builder()
                .id(profile.getId())
                .professionalTitle(profile.getProfessionalTitle())
                .professionalSummary(profile.getProfessionalSummary())
                .objectives(new LinkedHashSet<>(profile.getObjectives()))
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();

        Set<SkillPreferenceJpaEntity> skillEntities = new LinkedHashSet<>();
        for (SkillPreference skillPreference : profile.getSkillPreferences()) {
            skillEntities.add(SkillPreferenceJpaEntity.builder()
                    .id(skillPreference.getId())
                    .userProfile(entity)
                    .skill(skillPreference.getSkill())
                    .weight(skillPreference.getWeight())
                    .createdAt(skillPreference.getCreatedAt())
                    .build());
        }
        entity.setSkills(skillEntities);
        return entity;
    }

    public UserProfile toDomain(UserProfileJpaEntity entity) {
        Set<SkillPreference> skills = (entity.getSkills() == null ? Set.<SkillPreferenceJpaEntity>of() : entity.getSkills()).stream()
                .map(skill -> SkillPreference.restore(
                        skill.getId(),
                        entity.getId(),
                        skill.getSkill(),
                        skill.getWeight(),
                        skill.getCreatedAt()
                ))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        Set<com.dunkweb.talentbridge.domain.enums.CareerObjective> objectives =
                entity.getObjectives() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(entity.getObjectives());

        return UserProfile.restore(
                entity.getId(),
                entity.getProfessionalTitle(),
                entity.getProfessionalSummary(),
                objectives,
                skills,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
