package com.dunkweb.talentbridge.application.dto;

import java.time.LocalDateTime;

public record ProviderAnalyticsResponse(
        String provider,
        long executions,
        long successfulExecutions,
        long failedExecutions,
        long candidatesFound,
        long candidatesImported,
        long duplicatesIgnored,
        long averageExecutionTimeMs,
        double averageScore,
        LocalDateTime lastExecutionAt
) {
}
