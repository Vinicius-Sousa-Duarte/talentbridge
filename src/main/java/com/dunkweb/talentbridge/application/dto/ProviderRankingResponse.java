package com.dunkweb.talentbridge.application.dto;

public record ProviderRankingResponse(
        String provider,
        double averageScore
) {
}
