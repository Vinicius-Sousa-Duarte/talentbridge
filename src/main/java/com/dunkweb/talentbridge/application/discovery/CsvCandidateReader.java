package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER;

@Component
@Slf4j
public class CsvCandidateReader {

    private static final List<String> REQUIRED_COLUMNS = List.of(
            "name",
            "position",
            "company",
            "location",
            "headline",
            "linkedinurl"
    );

    public CsvImportResult read(String csvContent) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new ValidationException("Arquivo CSV vazio ou invalido.");
        }

        List<String> lines = csvContent.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        if (lines.isEmpty()) {
            throw new ValidationException("Arquivo CSV vazio.");
        }

        Map<String, Integer> header = parseHeader(lines.get(0));
        validateRequiredColumns(header);

        int rowsProcessed = 0;
        int rowsImported = 0;
        int rowsInvalid = 0;
        List<String> warnings = new ArrayList<>();
        List<RecruiterCandidate> candidates = new ArrayList<>();

        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            int rowNumber = lineIndex + 1;
            String line = lines.get(lineIndex);
            rowsProcessed++;

            String[] columns = line.split(",", -1);
            RecruiterCandidate candidate = parseRow(columns, header, rowNumber);
            if (candidate == null) {
                rowsInvalid++;
                warnings.add("Row " + rowNumber + " ignored due invalid or missing required fields");
                log.warn("{} Invalid row ignored provider=CSV row={}", PROVIDER, rowNumber);
                continue;
            }

            rowsImported++;
            candidates.add(candidate);
            log.debug("{} CSV row processed provider=CSV row={} sourceReference={}",
                    PROVIDER,
                    rowNumber,
                    candidate.sourceReference());
        }

        return new CsvImportResult(rowsProcessed, rowsImported, rowsInvalid, candidates, warnings);
    }

    private Map<String, Integer> parseHeader(String headerLine) {
        String[] tokens = headerLine.split(",", -1);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < tokens.length; i++) {
            String key = normalize(tokens[i]);
            if (!key.isBlank()) {
                headerIndex.putIfAbsent(key, i);
            }
        }
        return headerIndex;
    }

    private void validateRequiredColumns(Map<String, Integer> header) {
        List<String> missing = REQUIRED_COLUMNS.stream()
                .filter(required -> !header.containsKey(required))
                .toList();

        if (!missing.isEmpty()) {
            throw new ValidationException("CSV invalido. Colunas obrigatorias ausentes: " + String.join(", ", missing));
        }
    }

    private RecruiterCandidate parseRow(String[] columns, Map<String, Integer> header, int rowNumber) {
        String name = value(columns, header.get("name"));
        String position = value(columns, header.get("position"));
        String company = value(columns, header.get("company"));
        String location = value(columns, header.get("location"));
        String headline = value(columns, header.get("headline"));
        String linkedinUrl = value(columns, header.get("linkedinurl"));

        if (isBlank(name) || isBlank(position) || isBlank(company) || isBlank(linkedinUrl)) {
            return null;
        }

        Map<String, Object> rawEvidence = Map.of(
                "rowNumber", rowNumber,
                "line", String.join(",", columns)
        );

        return new RecruiterCandidate(
                UUID.randomUUID(),
                "CSV",
                DiscoverySource.CSV,
                String.valueOf(rowNumber),
                name,
                position,
                company,
                location,
                headline,
                linkedinUrl,
                0.8,
                Instant.now(),
                rawEvidence,
                Map.of()
        );
    }

    private String value(String[] columns, Integer index) {
        if (index == null || index < 0 || index >= columns.length) {
            return null;
        }
        String value = columns[index];
        return value == null ? null : value.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
