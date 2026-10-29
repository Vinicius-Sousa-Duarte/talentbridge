package com.dunkweb.talentbridge.application.dto;

public record RecruiterCompanyAnalyticsResponse(
        String company,
        long recruiterCount,
        double averageScore
) {
}
