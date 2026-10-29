package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;

import java.util.List;

public record ProviderCandidateBatch(
        List<RecruiterCandidate> candidates,
        int rowsProcessed,
        int rowsImported,
        int rowsInvalid,
        List<String> warnings,
        List<String> errors
) {
    public ProviderCandidateBatch {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        rowsProcessed = Math.max(0, rowsProcessed);
        rowsImported = Math.max(0, rowsImported);
        rowsInvalid = Math.max(0, rowsInvalid);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ProviderCandidateBatch empty() {
        return new ProviderCandidateBatch(List.of(), 0, 0, 0, List.of(), List.of());
    }
}
