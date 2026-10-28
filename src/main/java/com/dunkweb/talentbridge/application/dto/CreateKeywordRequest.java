package com.dunkweb.talentbridge.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateKeywordRequest(
        @NotBlank @Size(max = 160) String description
) {
}
