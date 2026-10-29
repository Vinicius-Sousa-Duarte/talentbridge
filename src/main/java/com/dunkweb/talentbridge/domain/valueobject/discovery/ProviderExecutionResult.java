package com.dunkweb.talentbridge.domain.valueobject.discovery;

import java.util.List;

public record ProviderExecutionResult(
        String providerName,
    Integer rowsProcessed,
    Integer rowsImported,
    Integer rowsInvalid,
        Integer candidatesFound,
        Integer candidatesImported,
        Integer duplicatesIgnored,
        Long executionTimeMs,
        Boolean successful,
        List<String> warnings,
        List<String> errors
) {
    public ProviderExecutionResult {
        providerName = providerName == null || providerName.isBlank() ? "UNKNOWN" : providerName.trim();
        rowsProcessed = rowsProcessed == null ? 0 : Math.max(0, rowsProcessed);
        rowsImported = rowsImported == null ? 0 : Math.max(0, rowsImported);
        rowsInvalid = rowsInvalid == null ? 0 : Math.max(0, rowsInvalid);
        candidatesFound = candidatesFound == null ? 0 : Math.max(0, candidatesFound);
        candidatesImported = candidatesImported == null ? 0 : Math.max(0, candidatesImported);
        duplicatesIgnored = duplicatesIgnored == null ? 0 : Math.max(0, duplicatesIgnored);
        executionTimeMs = executionTimeMs == null ? 0L : Math.max(0L, executionTimeMs);
        successful = successful != null && successful;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
