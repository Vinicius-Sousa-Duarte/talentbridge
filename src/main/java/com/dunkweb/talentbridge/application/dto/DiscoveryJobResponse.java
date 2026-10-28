package com.dunkweb.talentbridge.application.dto;

import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;

import java.util.UUID;

public record DiscoveryJobResponse(
        UUID jobId,
        Integer requestedProfiles,
        Integer profilesFound,
        Integer profilesImported,
        Integer duplicatesIgnored,
        Integer pagesVisited,
        Integer queriesExecuted,
        Long executionTimeMs,
        DiscoveryJobStatus status,
        String errorMessage
) {
}
