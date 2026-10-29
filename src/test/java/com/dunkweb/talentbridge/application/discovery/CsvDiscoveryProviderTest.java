package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvDiscoveryProviderTest {

    private final CsvDiscoveryProvider provider = new CsvDiscoveryProvider(new CsvCandidateReader());

    @Test
    void shouldSupportCsvContext() {
        DiscoveryContext context = contextWithCsv("name,position,company,location,headline,linkedinUrl\n");

        assertThat(provider.supports(context)).isTrue();
    }

    @Test
    void shouldNotSupportNonCsvContext() {
        DiscoveryContext context = new DiscoveryContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                List.of(),
                List.of(),
                10,
                10,
                1,
                Duration.ofSeconds(30),
                "corr",
                Map.of("discoverySource", DiscoverySource.MANUAL)
        );

        assertThat(provider.supports(context)).isFalse();
    }

    @Test
    void shouldImportValidCsvRows() {
        DiscoveryContext context = contextWithCsv(
                "name,position,company,location,headline,linkedinUrl\n"
                        + "Lucas Ferreira,Tech Recruiter,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n"
                        + "Mariana Souza,Talent Acquisition,Nubank,Sao Paulo,AWS Recruiting,https://linkedin.com/in/mariana\n"
        );

        ProviderCandidateBatch batch = provider.discover(context);

        assertThat(batch.rowsProcessed()).isEqualTo(2);
        assertThat(batch.rowsImported()).isEqualTo(2);
        assertThat(batch.rowsInvalid()).isZero();
        assertThat(batch.candidates()).hasSize(2);
    }

    @Test
    void shouldReportInvalidRows() {
        DiscoveryContext context = contextWithCsv(
                "name,position,company,location,headline,linkedinUrl\n"
                        + "Lucas Ferreira,,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n"
        );

        ProviderCandidateBatch batch = provider.discover(context);

        assertThat(batch.rowsProcessed()).isEqualTo(1);
        assertThat(batch.rowsImported()).isZero();
        assertThat(batch.rowsInvalid()).isEqualTo(1);
        assertThat(batch.warnings()).isNotEmpty();
    }

    @Test
    void shouldFailWhenCsvContentIsEmpty() {
        DiscoveryContext context = contextWithCsv("  ");

        assertThatThrownBy(() -> provider.discover(context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("CSV");
    }

    private DiscoveryContext contextWithCsv(String csvContent) {
        return new DiscoveryContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of("Java"),
                List.of("Tech Recruiter"),
                List.of("BACKEND_DEVELOPER"),
                20,
                20,
                1,
                Duration.ofSeconds(30),
                "corr-csv",
                Map.of(
                        "discoverySource", DiscoverySource.CSV,
                        "csvContent", csvContent
                )
        );
    }
}
