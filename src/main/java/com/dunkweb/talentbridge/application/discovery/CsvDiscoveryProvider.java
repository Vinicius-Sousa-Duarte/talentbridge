package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderCapability;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvDiscoveryProvider implements DiscoveryProvider {

    private final CsvCandidateReader csvCandidateReader;

    @Override
    public DiscoverySource source() {
        return DiscoverySource.CSV;
    }

    @Override
    public boolean supports(DiscoveryContext context) {
        Object requestedSource = context.metadata().get("discoverySource");
        if (requestedSource == null) {
            return false;
        }

        if (requestedSource instanceof DiscoverySource source) {
            return source == DiscoverySource.CSV;
        }

        return "CSV".equalsIgnoreCase(String.valueOf(requestedSource));
    }

    @Override
    public ProviderCandidateBatch discover(DiscoveryContext context) {
        log.info("{} CSV import started correlationId={}", PROVIDER, context.correlationId());

        Object csvContentRaw = context.metadata().get("csvContent");
        String csvContent = csvContentRaw == null ? null : String.valueOf(csvContentRaw);

        CsvImportResult importResult = csvCandidateReader.read(csvContent);

        log.info("{} CSV import completed correlationId={} rowsProcessed={} rowsImported={} rowsInvalid={}",
                PROVIDER,
                context.correlationId(),
                importResult.rowsProcessed(),
                importResult.rowsImported(),
                importResult.rowsInvalid());

        return new ProviderCandidateBatch(
                importResult.candidates(),
                importResult.rowsProcessed(),
                importResult.rowsImported(),
                importResult.rowsInvalid(),
                importResult.warnings(),
                List.of()
        );
    }

    @Override
    public boolean health() {
        return true;
    }

    @Override
    public List<ProviderCapability> capabilities() {
        return List.of(new ProviderCapability(DiscoverySource.CSV, false, false, java.util.Map.of()));
    }
}
