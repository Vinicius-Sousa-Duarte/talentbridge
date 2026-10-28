package com.dunkweb.talentbridge.application.dto;

import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record CreateUserProfileRequest(
        @NotBlank @Size(max = 160) String professionalTitle,
        @NotBlank @Size(max = 1000) String professionalSummary,
        Set<CareerObjective> objectives,
        @NotEmpty List<@Valid CreateSkillPreferenceRequest> skills
) {
}
