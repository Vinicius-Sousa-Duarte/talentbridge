package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderCapability;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER;

@Component
@Slf4j
public class ManualDiscoveryProvider implements DiscoveryProvider {

    private static final String SOURCE_REFERENCE = "MANUAL_ENTRY";

    @Override
    public DiscoverySource source() {
        return DiscoverySource.MANUAL;
    }

    @Override
    public boolean supports(DiscoveryContext context) {
        Object requestedSource = context.metadata().get("discoverySource");
        if (requestedSource == null) {
            return false;
        }

        if (requestedSource instanceof DiscoverySource source) {
            return source == DiscoverySource.MANUAL;
        }

        return "MANUAL".equalsIgnoreCase(String.valueOf(requestedSource));
    }

    @Override
    public ProviderCandidateBatch discover(DiscoveryContext context) {
        CreateManualCandidateRequest request = extractRequest(context);

        log.info("{} Manual candidate received correlationId={} provider={} candidateName={} sourceReference={}",
                PROVIDER,
                context.correlationId(),
                source().name(),
                request.name(),
                SOURCE_REFERENCE);

        Map<String, Object> rawEvidence = new LinkedHashMap<>();
        rawEvidence.put("name", request.name());
        if (request.position() != null) {
            rawEvidence.put("position", request.position());
        }
        if (request.company() != null) {
            rawEvidence.put("company", request.company());
        }
        if (request.location() != null) {
            rawEvidence.put("location", request.location());
        }
        if (request.headline() != null) {
            rawEvidence.put("headline", request.headline());
        }
        if (request.linkedinUrl() != null) {
            rawEvidence.put("linkedinUrl", request.linkedinUrl());
        }

        RecruiterCandidate candidate = new RecruiterCandidate(
                null,
                source().name(),
                DiscoverySource.MANUAL,
                SOURCE_REFERENCE,
                request.name(),
                request.position(),
                request.company(),
                request.location(),
                request.headline(),
                request.linkedinUrl(),
                1.0,
                Instant.now(),
                rawEvidence,
                Map.of()
        );

        log.info("{} Manual candidate processed correlationId={} provider={} candidateName={} sourceReference={}",
                PROVIDER,
                context.correlationId(),
                source().name(),
                candidate.name(),
                SOURCE_REFERENCE);

        return new ProviderCandidateBatch(
                List.of(candidate),
                1,
                1,
                0,
                List.of(),
                List.of()
        );
    }

    @Override
    public boolean health() {
        return true;
    }

    @Override
    public List<ProviderCapability> capabilities() {
        return List.of(new ProviderCapability(DiscoverySource.MANUAL, false, false, Map.of()));
    }

    private CreateManualCandidateRequest extractRequest(DiscoveryContext context) {
        Object rawRequest = context.metadata().get("manualCandidate");
        if (rawRequest instanceof CreateManualCandidateRequest request) {
            if (request.name() == null || request.name().isBlank()) {
                throw new ValidationException("Nome do recrutador e obrigatorio para discovery manual.");
            }
            return request;
        }

        throw new ValidationException("Payload manual invalido para discovery.");
    }
}
