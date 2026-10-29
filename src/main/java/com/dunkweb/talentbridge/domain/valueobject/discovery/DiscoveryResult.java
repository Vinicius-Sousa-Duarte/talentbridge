package com.dunkweb.talentbridge.domain.valueobject.discovery;

import java.util.List;
import java.util.UUID;

public record DiscoveryResult(
        UUID jobId,
        Integer totalCandidatesFound,
        Integer uniqueCandidatesFound,
        Integer importedCandidates,
        Integer duplicatesIgnored,
        Long executionTimeMs,
        List<ProviderExecutionResult> providerResults,
        List<RecruiterCandidate> candidates
) {
    public DiscoveryResult {
        jobId = jobId == null ? UUID.randomUUID() : jobId;
        totalCandidatesFound = totalCandidatesFound == null ? 0 : Math.max(0, totalCandidatesFound);
        uniqueCandidatesFound = uniqueCandidatesFound == null ? 0 : Math.max(0, uniqueCandidatesFound);
        importedCandidates = importedCandidates == null ? 0 : Math.max(0, importedCandidates);
        duplicatesIgnored = duplicatesIgnored == null ? 0 : Math.max(0, duplicatesIgnored);
        executionTimeMs = executionTimeMs == null ? 0L : Math.max(0L, executionTimeMs);
        providerResults = providerResults == null ? List.of() : List.copyOf(providerResults);
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
