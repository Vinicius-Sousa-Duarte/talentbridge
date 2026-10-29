package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.application.service.ProviderStatisticsService;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderExecutionResult;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DEDUPLICATION;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.DISCOVERY;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER_ANALYTICS;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.SCORING;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscoveryOrchestrator {

    private final DiscoveryProviderRegistry providerRegistry;
    private final CandidateNormalizer candidateNormalizer;
    private final CandidateDeduplicationService candidateDeduplicationService;
    private final RecruiterScoringEngine recruiterScoringEngine;
    private final RecruiterRepository recruiterRepository;
    private final UserProfileRepository userProfileRepository;
        private final ProviderStatisticsService providerStatisticsService;

    public DiscoveryResult execute(DiscoveryContext context) {
        long startedAt = System.nanoTime();
        UUID jobId = UUID.randomUUID();

        log.info("{} Discovery started jobId={} correlationId={}", DISCOVERY, jobId, context.correlationId());

        List<DiscoveryProvider> selectedProviders = providerRegistry.listProviders().stream()
                .filter(provider -> provider.supports(context))
                .toList();

        if (selectedProviders.isEmpty()) {
            log.info("{} Discovery completed jobId={} reason=no-supported-providers", DISCOVERY, jobId);
            return new DiscoveryResult(jobId, 0, 0, 0, 0, elapsedMs(startedAt), List.of(), List.of());
        }

        List<ProviderExecutionResult> providerResults = new ArrayList<>();
        Map<String, List<RecruiterCandidate>> candidatesByProvider = new LinkedHashMap<>();
        List<RecruiterCandidate> allCandidates = new ArrayList<>();

        for (DiscoveryProvider provider : selectedProviders) {
            String providerName = provider.source().name();
            long providerStartedAt = System.nanoTime();

            log.info("{} Executing provider {} jobId={}", PROVIDER, providerName, jobId);

            if (!provider.health()) {
                log.warn("{} Provider unavailable provider={} jobId={}", PROVIDER, providerName, jobId);
                providerResults.add(new ProviderExecutionResult(
                        providerName,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        elapsedMs(providerStartedAt),
                        false,
                        List.of("Provider unavailable"),
                        List.of()
                ));
                continue;
            }

            try {
                ProviderCandidateBatch batch = provider.discover(context);
                List<RecruiterCandidate> normalized = (batch == null ? ProviderCandidateBatch.empty() : batch).candidates().stream()
                        .map(candidate -> candidateNormalizer.normalize(candidate, context))
                        .filter(candidate -> candidate != null)
                        .toList();

                candidatesByProvider.put(providerName, normalized);
                allCandidates.addAll(normalized);

                providerResults.add(new ProviderExecutionResult(
                        providerName,
                        batch == null ? 0 : batch.rowsProcessed(),
                        batch == null ? 0 : batch.rowsImported(),
                        batch == null ? 0 : batch.rowsInvalid(),
                        normalized.size(),
                        0,
                        0,
                        elapsedMs(providerStartedAt),
                        true,
                        batch == null ? List.of() : batch.warnings(),
                        batch == null ? List.of() : batch.errors()
                ));

                log.info("{} Provider completed provider={} jobId={} candidatesFound={}",
                        PROVIDER,
                        providerName,
                        jobId,
                        normalized.size());
            } catch (Exception exception) {
                log.error("{} Provider failed provider={} jobId={} message={}",
                        PROVIDER,
                        providerName,
                        jobId,
                        exception.getMessage(),
                        exception);

                providerResults.add(new ProviderExecutionResult(
                        providerName,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        elapsedMs(providerStartedAt),
                        false,
                        List.of(),
                        List.of(exception.getMessage() == null ? "Provider error" : exception.getMessage())
                ));
            }
        }

        CandidateDeduplicationResult deduplicationResult = candidateDeduplicationService.deduplicate(allCandidates);
        List<RecruiterCandidate> uniqueCandidates = deduplicationResult.uniqueCandidates();

        log.info("{} Deduplication completed jobId={} totalCandidates={} uniqueCandidates={} duplicatesIgnored={}",
                DEDUPLICATION,
                jobId,
                allCandidates.size(),
                uniqueCandidates.size(),
                deduplicationResult.duplicatesIgnored());

        UserProfile activeProfile = userProfileRepository.findActiveProfile().orElse(null);
        List<RecruiterCandidate> scoredCandidates = new ArrayList<>();
        Set<UUID> importedCandidateIds = new java.util.HashSet<>();
        Map<String, Integer> persistedDuplicateByProvider = new HashMap<>();
        int persistedDuplicatesIgnored = 0;

        for (RecruiterCandidate candidate : uniqueCandidates) {
            int calculatedScore = recruiterScoringEngine.calculate(
                    activeProfile,
                    candidate.headline(),
                    safe(candidate.position()) + " " + safe(candidate.company()),
                    null
            );

            log.debug("{} Candidate scored provider={} sourceReference={} score={}",
                    SCORING,
                    candidate.provider(),
                    candidate.sourceReference(),
                    calculatedScore);

            Map<String, Object> normalizedFields = new HashMap<>(candidate.normalizedFields());
            normalizedFields.put("score", calculatedScore);

            RecruiterCandidate scored = new RecruiterCandidate(
                    candidate.temporaryId(),
                    candidate.provider(),
                    candidate.source(),
                    candidate.sourceReference(),
                    candidate.name(),
                    candidate.position(),
                    candidate.company(),
                    candidate.location(),
                    candidate.headline(),
                    candidate.linkedinUrl(),
                    candidate.confidenceScore(),
                    candidate.discoveredAt(),
                    candidate.rawEvidence(),
                    normalizedFields
            );
            scoredCandidates.add(scored);

            if (!isPersistable(scored)) {
                continue;
            }

                        if (alreadyExistsInRepository(scored)) {
                                persistedDuplicatesIgnored++;
                                persistedDuplicateByProvider.merge(scored.provider(), 1, Integer::sum);
                                log.info("{} Duplicate candidate ignored because it already exists provider={} sourceReference={} linkedinUrl={} name={} company={}",
                                                DEDUPLICATION,
                                                scored.provider(),
                                                scored.sourceReference(),
                                                scored.linkedinUrl(),
                                                scored.name(),
                                                scored.company());
                                continue;
                        }

            Recruiter recruiter = Recruiter.create(
                    scored.name(),
                    scored.position(),
                    scored.company(),
                    scored.location(),
                    scored.linkedinUrl(),
                    scored.headline(),
                    null,
                    calculatedScore
            );
            recruiterRepository.save(recruiter);
            importedCandidateIds.add(scored.temporaryId());
        }

        Map<String, Integer> importedByProvider = scoredCandidates.stream()
                .filter(candidate -> importedCandidateIds.contains(candidate.temporaryId()))
                .collect(Collectors.groupingBy(RecruiterCandidate::provider, Collectors.summingInt(candidate -> 1)));

        List<ProviderExecutionResult> finalizedProviderResults = providerResults.stream()
                .map(result -> {
                    int candidatesFound = result.candidatesFound();
                    int imported = importedByProvider.getOrDefault(result.providerName(), 0);
                                        int duplicates = deduplicationResult.duplicatesByProvider().getOrDefault(result.providerName(), 0)
                                                        + persistedDuplicateByProvider.getOrDefault(result.providerName(), 0);

                    return new ProviderExecutionResult(
                            result.providerName(),
                            result.rowsProcessed(),
                            result.rowsImported(),
                            result.rowsInvalid(),
                            candidatesFound,
                            imported,
                            duplicates,
                            result.executionTimeMs(),
                            result.successful(),
                            result.warnings(),
                            result.errors()
                    );
                })
                .toList();

        DiscoveryResult result = new DiscoveryResult(
                jobId,
                allCandidates.size(),
                uniqueCandidates.size(),
                importedCandidateIds.size(),
                deduplicationResult.duplicatesIgnored() + persistedDuplicatesIgnored,
                elapsedMs(startedAt),
                finalizedProviderResults,
                scoredCandidates
        );

        log.info("{} Discovery completed jobId={} totalCandidatesFound={} uniqueCandidatesFound={} importedCandidates={} duplicatesIgnored={} executionTimeMs={}",
                DISCOVERY,
                result.jobId(),
                result.totalCandidatesFound(),
                result.uniqueCandidatesFound(),
                result.importedCandidates(),
                result.duplicatesIgnored() + persistedDuplicatesIgnored,
                result.executionTimeMs());

        try {
            providerStatisticsService.updateFromExecution(
                    result.providerResults(),
                    result.candidates(),
                    result.jobId().toString()
            );
        } catch (Exception exception) {
            log.warn("{} Failed to update provider analytics jobId={} message={}",
                    PROVIDER_ANALYTICS,
                    result.jobId(),
                    exception.getMessage(),
                    exception);
        }

        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isPersistable(RecruiterCandidate candidate) {
        return candidate != null
                && candidate.name() != null && !candidate.name().isBlank()
                && candidate.position() != null && !candidate.position().isBlank()
                && candidate.company() != null && !candidate.company().isBlank()
                && candidate.linkedinUrl() != null && !candidate.linkedinUrl().isBlank();
    }

        private boolean alreadyExistsInRepository(RecruiterCandidate candidate) {
                return recruiterRepository.existsByLinkedinUrl(candidate.linkedinUrl())
                                || recruiterRepository.existsByNameAndCompany(candidate.name(), candidate.company());
        }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
