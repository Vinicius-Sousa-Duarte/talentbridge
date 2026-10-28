package com.dunkweb.talentbridge.domain.entity;

import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfile {

    private UUID id;
    private String professionalTitle;
    private String professionalSummary;
    @Builder.Default
    private Set<CareerObjective> objectives = new LinkedHashSet<>();
    @Builder.Default
    private Set<SkillPreference> skillPreferences = new LinkedHashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfile create(
            String professionalTitle,
            String professionalSummary,
            Collection<CareerObjective> objectives,
            Collection<SkillInput> skills
    ) {
        validateRequired(professionalTitle, "Titulo profissional");
        validateRequired(professionalSummary, "Resumo profissional");

        UUID profileId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UserProfile profile = UserProfile.builder()
                .id(profileId)
                .professionalTitle(professionalTitle.trim())
                .professionalSummary(professionalSummary.trim())
                .objectives(sanitizeObjectives(objectives))
                .skillPreferences(new LinkedHashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        profile.replaceSkills(skills);
        return profile;
    }

    public static UserProfile restore(
            UUID id,
            String professionalTitle,
            String professionalSummary,
            Set<CareerObjective> objectives,
            Set<SkillPreference> skillPreferences,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return UserProfile.builder()
                .id(id)
                .professionalTitle(professionalTitle)
                .professionalSummary(professionalSummary)
                .objectives(objectives == null ? new LinkedHashSet<>() : new LinkedHashSet<>(objectives))
                .skillPreferences(skillPreferences == null ? new LinkedHashSet<>() : new LinkedHashSet<>(skillPreferences))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void update(
            String professionalTitle,
            String professionalSummary,
            Collection<CareerObjective> objectives,
            Collection<SkillInput> skills
    ) {
        validateRequired(professionalTitle, "Titulo profissional");
        validateRequired(professionalSummary, "Resumo profissional");

        this.professionalTitle = professionalTitle.trim();
        this.professionalSummary = professionalSummary.trim();
        this.objectives = sanitizeObjectives(objectives);
        replaceSkills(skills);
        this.updatedAt = LocalDateTime.now();
    }

    private void replaceSkills(Collection<SkillInput> skills) {
        if (skills == null || skills.isEmpty()) {
            this.skillPreferences = new LinkedHashSet<>();
            return;
        }

        Set<SkillPreference> generated = new LinkedHashSet<>();
        for (SkillInput input : skills) {
            generated.add(SkillPreference.create(this.id, input.skill(), input.weight()));
        }

        this.skillPreferences = generated;
    }

    private static Set<CareerObjective> sanitizeObjectives(Collection<CareerObjective> objectives) {
        if (objectives == null || objectives.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(objectives);
    }

    private static void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " e obrigatorio.");
        }
    }

    public record SkillInput(String skill, Integer weight) {
    }
}
