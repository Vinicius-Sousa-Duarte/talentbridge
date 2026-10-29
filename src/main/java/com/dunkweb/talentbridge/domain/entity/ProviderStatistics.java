package com.dunkweb.talentbridge.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProviderStatistics(
        UUID id,
        String providerName,
        long executions,
        long successfulExecutions,
        long failedExecutions,
        long candidatesFound,
        long candidatesImported,
        long duplicatesIgnored,
        long totalExecutionTimeMs,
        double totalScore,
        long scoreSamples,
        double averageExecutionTimeMs,
        double averageScore,
        LocalDateTime lastExecutionAt
) {

    public ProviderStatistics {
        id = id == null ? UUID.randomUUID() : id;
        providerName = providerName == null || providerName.isBlank() ? "UNKNOWN" : providerName.trim().toUpperCase();
        executions = Math.max(0, executions);
        successfulExecutions = Math.max(0, successfulExecutions);
        failedExecutions = Math.max(0, failedExecutions);
        candidatesFound = Math.max(0, candidatesFound);
        candidatesImported = Math.max(0, candidatesImported);
        duplicatesIgnored = Math.max(0, duplicatesIgnored);
        totalExecutionTimeMs = Math.max(0, totalExecutionTimeMs);
        totalScore = Math.max(0.0, totalScore);
        scoreSamples = Math.max(0, scoreSamples);
        averageExecutionTimeMs = Math.max(0.0, averageExecutionTimeMs);
        averageScore = Math.max(0.0, averageScore);
    }

    public static ProviderStatistics empty(String providerName) {
        return new ProviderStatistics(
                null,
                providerName,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0.0,
                0,
                0.0,
                0.0,
                null
        );
    }

    public ProviderStatistics recordExecution(
            boolean successful,
            int found,
            int imported,
            int duplicates,
            long executionTimeMs,
            double scoreSum,
            long scoreCount,
            LocalDateTime executedAt
    ) {
        long updatedExecutions = executions + 1;
        long updatedSuccessful = successfulExecutions + (successful ? 1 : 0);
        long updatedFailed = failedExecutions + (successful ? 0 : 1);
        long updatedFound = candidatesFound + Math.max(0, found);
        long updatedImported = candidatesImported + Math.max(0, imported);
        long updatedDuplicates = duplicatesIgnored + Math.max(0, duplicates);
        long updatedTotalTime = totalExecutionTimeMs + Math.max(0, executionTimeMs);
        double updatedTotalScore = totalScore + Math.max(0.0, scoreSum);
        long updatedScoreSamples = scoreSamples + Math.max(0, scoreCount);

        double updatedAverageTime = updatedExecutions == 0 ? 0.0 : (double) updatedTotalTime / updatedExecutions;
        double updatedAverageScore = updatedScoreSamples == 0 ? 0.0 : updatedTotalScore / updatedScoreSamples;

        return new ProviderStatistics(
                id,
                providerName,
                updatedExecutions,
                updatedSuccessful,
                updatedFailed,
                updatedFound,
                updatedImported,
                updatedDuplicates,
                updatedTotalTime,
                updatedTotalScore,
                updatedScoreSamples,
                updatedAverageTime,
                updatedAverageScore,
                executedAt
        );
    }
}
