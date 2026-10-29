package com.dunkweb.talentbridge.domain.valueobject.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RecruiterCandidate(
        UUID temporaryId,
        String provider,
        DiscoverySource source,
        String sourceReference,
        String name,
        String position,
        String company,
        String location,
        String headline,
        String linkedinUrl,
        Double confidenceScore,
        Instant discoveredAt,
        Map<String, Object> rawEvidence,
        Map<String, Object> normalizedFields
) {
    public RecruiterCandidate {
        temporaryId = temporaryId == null ? UUID.randomUUID() : temporaryId;
        provider = provider == null || provider.isBlank() ? "UNKNOWN" : provider.trim();
        source = source == null ? DiscoverySource.UNKNOWN : source;
        sourceReference = sourceReference == null ? "N/A" : sourceReference.trim();
        discoveredAt = discoveredAt == null ? Instant.now() : discoveredAt;
        rawEvidence = rawEvidence == null ? Map.of() : Map.copyOf(rawEvidence);
        normalizedFields = normalizedFields == null ? Map.of() : Map.copyOf(normalizedFields);
    }
}
