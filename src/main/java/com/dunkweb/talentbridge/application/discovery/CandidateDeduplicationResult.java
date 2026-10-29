package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;

import java.util.List;
import java.util.Map;

public record CandidateDeduplicationResult(
        List<RecruiterCandidate> uniqueCandidates,
        int duplicatesIgnored,
        Map<String, Integer> duplicatesByProvider
) {
    public CandidateDeduplicationResult {
        uniqueCandidates = uniqueCandidates == null ? List.of() : List.copyOf(uniqueCandidates);
        duplicatesIgnored = Math.max(0, duplicatesIgnored);
        duplicatesByProvider = duplicatesByProvider == null ? Map.of() : Map.copyOf(duplicatesByProvider);
    }
}
