package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.application.port.output.DiscoveryExtractionResult;
import com.dunkweb.talentbridge.application.port.output.RecruiterDiscoveryGateway;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DISCOVERY;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.ERROR;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROFILE;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.SEARCH;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "talentbridge.discovery", name = "provider", havingValue = "playwright", matchIfMissing = true)
public class PlaywrightRecruiterDiscoveryGateway implements RecruiterDiscoveryGateway {

    private static final int MAX_TIMEOUT_ATTEMPTS_PER_QUERY = 2;

    private final DiscoveryProperties discoveryProperties;
    private final RecruiterSearchQueryBuilder queryBuilder;
    private final LinkedInSessionManager linkedInSessionManager;
    private final DiscoveryFailureClassifier discoveryFailureClassifier;
    private final BackoffPolicy backoffPolicy;

    @Override
    public DiscoveryExtractionResult discoverRecruiters(UserProfile profile, int desiredProfiles, UUID jobId) {
        if (!discoveryProperties.enabled()) {
            log.info("{} Discovery disabled by configuration jobId={}", DISCOVERY, jobId);
            return new DiscoveryExtractionResult(List.of(), 0, 0);
        }

        if (profile == null) {
            log.info("{} Discovery skipped because no active profile was provided jobId={}", DISCOVERY, jobId);
            return new DiscoveryExtractionResult(List.of(), 0, 0);
        }

        int maxQueriesPerExecution = Math.max(1, discoveryProperties.maxQueriesPerExecution());
        List<String> queries = queryBuilder.buildQueries(profile, maxQueriesPerExecution);
        if (queries.isEmpty()) {
            log.info("{} No search queries generated for profile jobId={}", DISCOVERY, jobId);
            return new DiscoveryExtractionResult(List.of(), 0, 0);
        }

        int maxProfilesPerExecution = Math.max(1, discoveryProperties.maxProfilesPerExecution());
        int extractedLimit = Math.min(Math.min(500, maxProfilesPerExecution), Math.max(1, desiredProfiles));

        try {
            return linkedInSessionManager.withAuthenticatedClient(jobId, client -> {
                List<RecruiterProfile> extractedProfiles = new ArrayList<>();
                int pagesVisited = 0;
                int queriesExecuted = 0;

                for (String query : queries) {
                    if (extractedProfiles.size() >= extractedLimit) {
                        log.warn("{} Extraction limit reached jobId={} extractedProfiles={} extractedLimit={}",
                                DISCOVERY,
                                jobId,
                                extractedProfiles.size(),
                                extractedLimit);
                        break;
                    }

                    log.info("{} Query execution started jobId={} query={}", SEARCH, jobId, query);
                    queriesExecuted++;

                    try {
                        QueryProcessingMetrics metrics = processQueryWithRetry(
                                client,
                                query,
                                extractedProfiles,
                                extractedLimit,
                                jobId
                        );
                        pagesVisited += metrics.pagesVisited();
                        log.info("{} Query execution completed jobId={} query={} pagesVisited={} queryExecutionTimeMs={}",
                                SEARCH,
                                jobId,
                                query,
                                metrics.pagesVisited(),
                                metrics.executionTimeMs());
                    } catch (DiscoveryExecutionException exception) {
                        if (isBlockingFailure(exception.getFailureType())) {
                            log.error("{} Blocking failure detected jobId={} query={} failureType={} message={}",
                                    ERROR,
                                    jobId,
                                    query,
                                    exception.getFailureType(),
                                    exception.getMessage());
                            throw exception;
                        }

                        log.error("{} Query processing exhausted retries jobId={} query={} failureType={} message={}",
                                ERROR,
                                jobId,
                                query,
                                exception.getFailureType(),
                                exception.getMessage());
                    }
                }

                return new DiscoveryExtractionResult(extractedProfiles, queriesExecuted, pagesVisited);
            });
        } catch (DiscoveryExecutionException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            DiscoveryFailureType failureType = discoveryFailureClassifier.classify(exception);
            throw new DiscoveryExecutionException(
                    failureType,
                    "LinkedIn session bootstrap failed before query execution.",
                    exception
            );
        }
    }

    private QueryProcessingMetrics processQueryWithRetry(
            LinkedInPageClient client,
            String query,
            List<RecruiterProfile> extractedProfiles,
            int extractedLimit,
            UUID jobId
    ) {
        DiscoveryFailureType lastFailureType = DiscoveryFailureType.UNKNOWN;
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= backoffPolicy.maxAttempts(); attempt++) {
            try {
                return processQuery(client, query, extractedProfiles, extractedLimit, jobId);
            } catch (RuntimeException exception) {
                lastException = exception;
                lastFailureType = discoveryFailureClassifier.classify(exception);

                if (isBlockingFailure(lastFailureType)) {
                    throw new DiscoveryExecutionException(
                            lastFailureType,
                            "Blocking signal detected during discovery query processing.",
                            exception
                    );
                }

                if (lastFailureType == DiscoveryFailureType.TIMEOUT && attempt >= MAX_TIMEOUT_ATTEMPTS_PER_QUERY) {
                    log.warn("{} Timeout retry budget exhausted jobId={} query={} attempt={} maxTimeoutAttempts={}",
                        SEARCH,
                        jobId,
                        query,
                        attempt,
                        MAX_TIMEOUT_ATTEMPTS_PER_QUERY);
                    break;
                }

                log.warn("{} Query attempt failed jobId={} query={} attempt={} failureType={} message={}",
                        SEARCH,
                        jobId,
                        query,
                        attempt,
                        lastFailureType,
                        exception.getMessage());

                if (attempt < backoffPolicy.maxAttempts()) {
                    backoffPolicy.sleepBeforeRetry(attempt);
                }
            }
        }

        throw new DiscoveryExecutionException(
                lastFailureType,
                "Query failed after maximum retry attempts.",
                lastException
        );
    }

    private QueryProcessingMetrics processQuery(
            LinkedInPageClient client,
            String query,
            List<RecruiterProfile> extractedProfiles,
            int extractedLimit,
            UUID jobId
    ) {
        long queryStartedAt = System.nanoTime();
        client.openSearch(query);

        int page = 1;
        int pagesVisited = 0;
        boolean hasNextPage = true;
        while (hasNextPage
                && page <= discoveryProperties.maxPagesPerSearch()
                && extractedProfiles.size() < extractedLimit) {

            List<RecruiterProfile> pageProfiles = client.extractRecruitersFromCurrentPage();
            extractedProfiles.addAll(pageProfiles);
            pagesVisited++;

            log.info("{} Search page processed jobId={} query={} page={} profilesFound={} totalExtracted={}",
                    SEARCH,
                    jobId,
                    query,
                    page,
                    pageProfiles.size(),
                    extractedProfiles.size());

            for (RecruiterProfile extractedProfile : pageProfiles) {
                log.debug("{} Recruiter extracted jobId={} query={} recruiterName={} linkedinUrl={}",
                        PROFILE,
                        jobId,
                        query,
                        extractedProfile.name(),
                        extractedProfile.linkedinUrl());
            }

            if (extractedProfiles.size() >= extractedLimit) {
                break;
            }

            hasNextPage = client.goToNextPage();
            page++;
        }

        long executionTimeMs = (System.nanoTime() - queryStartedAt) / 1_000_000;
        return new QueryProcessingMetrics(pagesVisited, executionTimeMs);
    }

    private boolean isBlockingFailure(DiscoveryFailureType failureType) {
        return failureType == DiscoveryFailureType.CAPTCHA || failureType == DiscoveryFailureType.RATE_LIMIT;
    }

    private record QueryProcessingMetrics(int pagesVisited, long executionTimeMs) {
    }
}
