package com.dunkweb.talentbridge.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSkillPreferenceRequest(
        @NotBlank @Size(max = 120) String skill,
        @Positive Integer weight
) {
}
