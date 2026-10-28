package com.dunkweb.talentbridge.application.dto;

import com.dunkweb.talentbridge.domain.enums.CareerObjective;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String professionalTitle,
        String professionalSummary,
        Set<CareerObjective> objectives,
        List<SkillPreferenceResponse> skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
