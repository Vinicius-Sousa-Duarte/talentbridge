package com.dunkweb.talentbridge.domain.valueobject.discovery;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DiscoveryContext(
        UUID userId,
        UUID profileId,
        List<String> skills,
        List<String> keywords,
        List<String> objectives,
        Integer desiredProfiles,
        Integer maxProfiles,
        Integer maxQueries,
        Duration timeout,
        String correlationId,
        Map<String, Object> metadata
) {
    public DiscoveryContext {
        skills = skills == null ? List.of() : List.copyOf(skills);
        keywords = keywords == null ? List.of() : List.copyOf(keywords);
        objectives = objectives == null ? List.of() : List.copyOf(objectives);
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        desiredProfiles = desiredProfiles == null ? 0 : Math.max(0, desiredProfiles);
        maxProfiles = maxProfiles == null ? 0 : Math.max(0, maxProfiles);
        maxQueries = maxQueries == null ? 0 : Math.max(0, maxQueries);
        correlationId = correlationId == null ? "N/A" : correlationId;
    }
}
