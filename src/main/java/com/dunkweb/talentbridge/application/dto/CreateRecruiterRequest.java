package com.dunkweb.talentbridge.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRecruiterRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 160) String position,
        @NotBlank @Size(max = 160) String company,
        @Size(max = 160) String location,
        @NotBlank @Size(max = 500) String linkedinUrl,
        @Size(max = 500) String headline,
        @Min(0) Integer connectionsCount
) {
}
