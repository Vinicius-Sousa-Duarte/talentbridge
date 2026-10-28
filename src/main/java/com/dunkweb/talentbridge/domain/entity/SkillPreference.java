package com.dunkweb.talentbridge.domain.entity;

import com.dunkweb.talentbridge.domain.exception.ValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SkillPreference {

    private UUID id;
    private UUID userProfileId;
    private String skill;
    private Integer weight;
    private LocalDateTime createdAt;

    public static SkillPreference create(UUID userProfileId, String skill, Integer weight) {
        validateRequiredId(userProfileId);
        validateSkill(skill);
        validateWeight(weight);

        return SkillPreference.builder()
                .id(UUID.randomUUID())
                .userProfileId(userProfileId)
                .skill(skill.trim())
                .weight(weight)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static SkillPreference restore(
            UUID id,
            UUID userProfileId,
            String skill,
            Integer weight,
            LocalDateTime createdAt
    ) {
        return SkillPreference.builder()
                .id(id)
                .userProfileId(userProfileId)
                .skill(skill)
                .weight(weight)
                .createdAt(createdAt)
                .build();
    }

    private static void validateRequiredId(UUID userProfileId) {
        if (userProfileId == null) {
            throw new ValidationException("User profile id e obrigatorio para skill preference.");
        }
    }

    private static void validateSkill(String skill) {
        if (skill == null || skill.isBlank()) {
            throw new ValidationException("Skill e obrigatoria.");
        }
    }

    private static void validateWeight(Integer weight) {
        if (weight == null || weight <= 0) {
            throw new ValidationException("Peso da skill deve ser maior que zero.");
        }
    }
}
