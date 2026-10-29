package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DEDUPLICATION;

@Component
@Slf4j
public class DefaultCandidateDeduplicationService implements CandidateDeduplicationService {

    @Override
    public CandidateDeduplicationResult deduplicate(List<RecruiterCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return new CandidateDeduplicationResult(List.of(), 0, Map.of());
        }

        List<RecruiterCandidate> unique = new ArrayList<>();
        Set<String> linkedinKeys = new HashSet<>();
        Set<String> nameCompanyKeys = new HashSet<>();
        Map<String, Integer> duplicatesByProvider = new HashMap<>();
        int duplicatesIgnored = 0;

        for (RecruiterCandidate candidate : candidates) {
            if (candidate == null) {
                continue;
            }

            String linkedinKey = normalize(candidate.linkedinUrl());
            String nameCompanyKey = normalize(candidate.name()) + "::" + normalize(candidate.company());

            boolean duplicateByLinkedin = linkedinKey != null && !linkedinKey.isBlank() && !"null".equals(linkedinKey)
                    && !linkedinKeys.add(linkedinKey);
            boolean duplicateByNameCompany = !"null::null".equals(nameCompanyKey) && !nameCompanyKeys.add(nameCompanyKey);

            if (duplicateByLinkedin || duplicateByNameCompany) {
                duplicatesIgnored++;
                duplicatesByProvider.merge(candidate.provider(), 1, Integer::sum);
                log.debug("{} Duplicate candidate ignored provider={} sourceReference={} duplicateByLinkedin={} duplicateByNameCompany={}",
                        DEDUPLICATION,
                        candidate.provider(),
                        candidate.sourceReference(),
                        duplicateByLinkedin,
                        duplicateByNameCompany);
                continue;
            }

            unique.add(candidate);
        }

        return new CandidateDeduplicationResult(unique, duplicatesIgnored, duplicatesByProvider);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "null";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
