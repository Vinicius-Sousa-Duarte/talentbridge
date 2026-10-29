package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;

import java.util.List;

public record CsvImportResult(
        int rowsProcessed,
        int rowsImported,
        int rowsInvalid,
        List<RecruiterCandidate> candidates,
        List<String> warnings
) {
    public CsvImportResult {
        rowsProcessed = Math.max(0, rowsProcessed);
        rowsImported = Math.max(0, rowsImported);
        rowsInvalid = Math.max(0, rowsInvalid);
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
