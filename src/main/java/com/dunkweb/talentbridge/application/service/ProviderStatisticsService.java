package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.ProviderStatisticsRepository;
import com.dunkweb.talentbridge.domain.entity.ProviderStatistics;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderExecutionResult;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER_ANALYTICS;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderStatisticsService {

    private final ProviderStatisticsRepository providerStatisticsRepository;

    public void updateFromExecution(
            List<ProviderExecutionResult> providerResults,
            List<RecruiterCandidate> scoredCandidates,
            String jobId
    ) {
        if (providerResults == null || providerResults.isEmpty()) {
            return;
        }

        Map<String, ScoreAggregate> scoreByProvider = aggregateScores(scoredCandidates);
        LocalDateTime now = LocalDateTime.now();

        for (ProviderExecutionResult result : providerResults) {
            String providerName = result.providerName();
            ProviderStatistics base = providerStatisticsRepository
                    .findByProviderName(providerName)
                    .orElse(ProviderStatistics.empty(providerName));

            ScoreAggregate aggregate = scoreByProvider.getOrDefault(providerName.toUpperCase(), ScoreAggregate.empty());

            ProviderStatistics updated = base.recordExecution(
                    result.successful(),
                    result.candidatesFound(),
                    result.candidatesImported(),
                    result.duplicatesIgnored(),
                    result.executionTimeMs(),
                    aggregate.scoreSum(),
                    aggregate.scoreCount(),
                    now
            );

            providerStatisticsRepository.save(updated);

            log.info("{} Statistics updated provider={} jobId={} executions={} avgScore={} avgExecutionMs={}",
                    PROVIDER_ANALYTICS,
                    updated.providerName(),
                    jobId,
                    updated.executions(),
                    updated.averageScore(),
                    updated.averageExecutionTimeMs());
        }
    }

    public List<ProviderStatistics> listAll() {
        return providerStatisticsRepository.findAll().stream()
                .sorted((left, right) -> left.providerName().compareToIgnoreCase(right.providerName()))
                .toList();
    }

    public ProviderStatistics getByProvider(String providerName) {
        return providerStatisticsRepository.findByProviderName(providerName)
                .orElse(ProviderStatistics.empty(providerName));
    }

    public List<ProviderStatistics> ranking() {
        List<ProviderStatistics> ranking = providerStatisticsRepository.findAll().stream()
                .sorted((left, right) -> {
                    int byScore = Double.compare(right.averageScore(), left.averageScore());
                    if (byScore != 0) {
                        return byScore;
                    }
                    return Long.compare(right.candidatesImported(), left.candidatesImported());
                })
                .toList();

        log.info("{} Ranking generated providers={}",
                PROVIDER_ANALYTICS,
                ranking.stream().map(ProviderStatistics::providerName).collect(Collectors.toList()));

        return ranking;
    }

    private Map<String, ScoreAggregate> aggregateScores(List<RecruiterCandidate> scoredCandidates) {
        if (scoredCandidates == null || scoredCandidates.isEmpty()) {
            return Map.of();
        }

        return scoredCandidates.stream()
                .collect(Collectors.groupingBy(
                        candidate -> candidate.provider().toUpperCase(),
                        Collectors.collectingAndThen(Collectors.toList(), candidates -> {
                            double scoreSum = 0.0;
                            long scoreCount = 0;

                            for (RecruiterCandidate candidate : candidates) {
                                Object rawScore = candidate.normalizedFields().get("score");
                                if (rawScore instanceof Number number) {
                                    scoreSum += number.doubleValue();
                                    scoreCount++;
                                }
                            }

                            return new ScoreAggregate(scoreSum, scoreCount);
                        })
                ));
    }

    private record ScoreAggregate(double scoreSum, long scoreCount) {
        private static ScoreAggregate empty() {
            return new ScoreAggregate(0.0, 0);
        }
    }
}
