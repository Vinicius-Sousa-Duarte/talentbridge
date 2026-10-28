package com.dunkweb.talentbridge.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SkillPreferenceResponse(
        UUID id,
        String skill,
        Integer weight,
        LocalDateTime createdAt
) {
}
