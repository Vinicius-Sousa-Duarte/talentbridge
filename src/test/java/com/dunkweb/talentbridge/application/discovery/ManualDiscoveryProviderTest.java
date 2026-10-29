package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualDiscoveryProviderTest {

    private final ManualDiscoveryProvider provider = new ManualDiscoveryProvider();

    @Test
    void shouldSupportManualContext() {
        DiscoveryContext context = contextWithManualRequest(new CreateManualCandidateRequest(
                "Lucas Ferreira",
                "Tech Recruiter",
                "Mercado Livre",
                "Sao Paulo",
                "Hiring Java Engineers",
                "https://linkedin.com/in/lucas"
        ));

        assertThat(provider.supports(context)).isTrue();
    }

    @Test
    void shouldTransformFullManualPayloadIntoCandidate() {
        DiscoveryContext context = contextWithManualRequest(new CreateManualCandidateRequest(
                "Lucas Ferreira",
                "Tech Recruiter",
                "Mercado Livre",
                "Sao Paulo",
                "Hiring Java Engineers",
                "https://linkedin.com/in/lucas"
        ));

        ProviderCandidateBatch batch = provider.discover(context);

        assertThat(batch.candidates()).hasSize(1);
        RecruiterCandidate candidate = batch.candidates().get(0);
        assertThat(candidate.provider()).isEqualTo("MANUAL");
        assertThat(candidate.source()).isEqualTo(DiscoverySource.MANUAL);
        assertThat(candidate.sourceReference()).isEqualTo("MANUAL_ENTRY");
        assertThat(candidate.name()).isEqualTo("Lucas Ferreira");
        assertThat(candidate.position()).isEqualTo("Tech Recruiter");
        assertThat(candidate.company()).isEqualTo("Mercado Livre");
        assertThat(candidate.linkedinUrl()).isEqualTo("https://linkedin.com/in/lucas");
        assertThat(candidate.discoveredAt()).isNotNull();
        assertThat(batch.rowsProcessed()).isEqualTo(1);
        assertThat(batch.rowsImported()).isEqualTo(1);
        assertThat(batch.rowsInvalid()).isZero();
    }

    @Test
    void shouldSupportMinimumPayloadWithOnlyName() {
        DiscoveryContext context = contextWithManualRequest(new CreateManualCandidateRequest(
                "Lucas Ferreira",
                null,
                null,
                null,
                null,
                null
        ));

        ProviderCandidateBatch batch = provider.discover(context);

        assertThat(batch.candidates()).hasSize(1);
        RecruiterCandidate candidate = batch.candidates().get(0);
        assertThat(candidate.name()).isEqualTo("Lucas Ferreira");
        assertThat(candidate.position()).isNull();
        assertThat(candidate.company()).isNull();
        assertThat(candidate.linkedinUrl()).isNull();
    }

    @Test
    void shouldFailWhenNameIsMissing() {
        DiscoveryContext context = contextWithManualRequest(new CreateManualCandidateRequest(
                " ",
                "Tech Recruiter",
                "Mercado Livre",
                "Sao Paulo",
                "Hiring Java Engineers",
                "https://linkedin.com/in/lucas"
        ));

        assertThatThrownBy(() -> provider.discover(context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Nome");
    }

    private DiscoveryContext contextWithManualRequest(CreateManualCandidateRequest request) {
        return new DiscoveryContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of("Java"),
                List.of("Tech Recruiter"),
                List.of("SOLUTIONS_ARCHITECT"),
                1,
                1,
                1,
                Duration.ofSeconds(30),
                "corr-manual",
                Map.of(
                        "discoverySource", DiscoverySource.MANUAL,
                        "manualCandidate", request
                )
        );
    }
}
