package com.dunkweb.talentbridge.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ExecuteRecruiterDiscoveryRequest(
        @Min(1) @Max(500) Integer desiredProfiles
) {
}
