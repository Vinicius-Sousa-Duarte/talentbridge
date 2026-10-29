package com.dunkweb.talentbridge.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateManualCandidateRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 160) String position,
        @Size(max = 160) String company,
        @Size(max = 160) String location,
        @Size(max = 500) String headline,
        @Size(max = 500) String linkedinUrl
) {
}
