package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.NORMALIZATION;

@Component
@Slf4j
public class DefaultCandidateNormalizer implements CandidateNormalizer {

    @Override
    public RecruiterCandidate normalize(RecruiterCandidate candidate, DiscoveryContext context) {
        if (candidate == null) {
            return null;
        }

        Map<String, Object> normalizedFields = new LinkedHashMap<>(candidate.normalizedFields());
        normalizedFields.put("name", normalizeText(candidate.name()));
        normalizedFields.put("position", normalizeText(candidate.position()));
        normalizedFields.put("company", normalizeText(candidate.company()));
        normalizedFields.put("location", normalizeText(candidate.location()));
        normalizedFields.put("headline", normalizeText(candidate.headline()));
        normalizedFields.put("linkedinUrl", normalizeLinkedinUrl(candidate.linkedinUrl()));

        log.debug("{} Candidate normalized provider={} sourceReference={} correlationId={}",
                NORMALIZATION,
                candidate.provider(),
                candidate.sourceReference(),
                context == null ? "N/A" : context.correlationId());

        return new RecruiterCandidate(
                candidate.temporaryId(),
                candidate.provider(),
                candidate.source(),
                candidate.sourceReference(),
                normalizeText(candidate.name()),
                normalizeText(candidate.position()),
                normalizeText(candidate.company()),
                normalizeText(candidate.location()),
                normalizeText(candidate.headline()),
                normalizeLinkedinUrl(candidate.linkedinUrl()),
                candidate.confidenceScore(),
                candidate.discoveredAt(),
                candidate.rawEvidence(),
                normalizedFields
        );
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeLinkedinUrl(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }

        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        return normalized;
    }
}
