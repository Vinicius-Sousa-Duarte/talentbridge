package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;

import java.util.List;

public record DiscoveryExtractionResult(
        List<RecruiterProfile> profiles,
        int queriesExecuted,
        int pagesVisited
) {
}
