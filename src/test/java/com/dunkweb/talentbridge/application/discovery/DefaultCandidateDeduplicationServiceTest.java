package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCandidateDeduplicationServiceTest {

    private final DefaultCandidateDeduplicationService service = new DefaultCandidateDeduplicationService();

    @Test
    void shouldKeepAllCandidatesWhenTheyAreUnique() {
        RecruiterCandidate first = candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana");
        RecruiterCandidate second = candidate("MANUAL", "2", "Bruno", "Globex", "https://linkedin.com/in/bruno");

        CandidateDeduplicationResult result = service.deduplicate(List.of(first, second));

        assertThat(result.uniqueCandidates()).hasSize(2);
        assertThat(result.duplicatesIgnored()).isZero();
    }

    @Test
    void shouldIgnoreDuplicatesByLinkedinUrl() {
        RecruiterCandidate first = candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana");
        RecruiterCandidate duplicate = candidate("MANUAL", "2", "Ana Oliveira", "Other", "https://linkedin.com/in/ana");

        CandidateDeduplicationResult result = service.deduplicate(List.of(first, duplicate));

        assertThat(result.uniqueCandidates()).hasSize(1);
        assertThat(result.duplicatesIgnored()).isEqualTo(1);
        assertThat(result.duplicatesByProvider()).containsEntry("MANUAL", 1);
    }

    @Test
    void shouldIgnoreDuplicatesByNameAndCompanyWhenLinkedinUrlIsMissing() {
        RecruiterCandidate first = candidate("CSV", "1", "Ana", "Acme", null);
        RecruiterCandidate duplicate = candidate("MANUAL", "2", " Ana ", " ACME ", null);

        CandidateDeduplicationResult result = service.deduplicate(List.of(first, duplicate));

        assertThat(result.uniqueCandidates()).hasSize(1);
        assertThat(result.duplicatesIgnored()).isEqualTo(1);
        assertThat(result.duplicatesByProvider()).containsEntry("MANUAL", 1);
    }

    private RecruiterCandidate candidate(
            String provider,
            String sourceRef,
            String name,
            String company,
            String linkedinUrl
    ) {
        return new RecruiterCandidate(
                UUID.randomUUID(),
                provider,
                DiscoverySource.UNKNOWN,
                sourceRef,
                name,
                "Tech Recruiter",
                company,
                "Sao Paulo",
                "Hiring Java",
                linkedinUrl,
                0.9,
                Instant.now(),
                Map.of(),
                Map.of()
        );
    }
}
