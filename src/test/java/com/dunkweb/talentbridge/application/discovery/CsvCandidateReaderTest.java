package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvCandidateReaderTest {

    private final CsvCandidateReader reader = new CsvCandidateReader();

    @Test
    void shouldReadValidCsv() {
        String csv = "name,position,company,location,headline,linkedinUrl\n"
                + "Lucas Ferreira,Tech Recruiter,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n";

        CsvImportResult result = reader.read(csv);

        assertThat(result.rowsProcessed()).isEqualTo(1);
        assertThat(result.rowsImported()).isEqualTo(1);
        assertThat(result.rowsInvalid()).isZero();
        assertThat(result.candidates()).hasSize(1);
        assertThat(result.candidates().get(0).provider()).isEqualTo("CSV");
        assertThat(result.candidates().get(0).sourceReference()).isEqualTo("2");
    }

    @Test
    void shouldFailWhenCsvIsEmpty() {
        assertThatThrownBy(() -> reader.read("  \n  "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("CSV");
    }

    @Test
    void shouldIgnoreInvalidRowWhenRequiredFieldsAreMissing() {
        String csv = "name,position,company,location,headline,linkedinUrl\n"
                + "Lucas Ferreira,,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n";

        CsvImportResult result = reader.read(csv);

        assertThat(result.rowsProcessed()).isEqualTo(1);
        assertThat(result.rowsImported()).isZero();
        assertThat(result.rowsInvalid()).isEqualTo(1);
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void shouldFailWhenRequiredColumnsAreMissing() {
        String csv = "name,position,company,location,headline\n"
                + "Lucas Ferreira,Tech Recruiter,Mercado Livre,Sao Paulo,Hiring Java Engineers\n";

        assertThatThrownBy(() -> reader.read(csv))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Colunas obrigatorias ausentes");
    }

    @Test
    void shouldKeepDuplicateRowsForDeduplicationPipeline() {
        String csv = "name,position,company,location,headline,linkedinUrl\n"
                + "Lucas Ferreira,Tech Recruiter,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n"
                + "Lucas Ferreira,Tech Recruiter,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n";

        CsvImportResult result = reader.read(csv);

        assertThat(result.rowsProcessed()).isEqualTo(2);
        assertThat(result.rowsImported()).isEqualTo(2);
        assertThat(result.rowsInvalid()).isZero();
        assertThat(result.candidates()).hasSize(2);
    }

    @Test
    void shouldReadMultipleRecruiters() {
        String csv = "name,position,company,location,headline,linkedinUrl\n"
                + "Lucas Ferreira,Tech Recruiter,Mercado Livre,Sao Paulo,Hiring Java Engineers,https://linkedin.com/in/lucas\n"
                + "Mariana Souza,Talent Acquisition,Nubank,Sao Paulo,AWS Recruiting,https://linkedin.com/in/mariana\n";

        CsvImportResult result = reader.read(csv);

        assertThat(result.rowsProcessed()).isEqualTo(2);
        assertThat(result.rowsImported()).isEqualTo(2);
        assertThat(result.rowsInvalid()).isZero();
        assertThat(result.candidates()).hasSize(2);
    }
}
