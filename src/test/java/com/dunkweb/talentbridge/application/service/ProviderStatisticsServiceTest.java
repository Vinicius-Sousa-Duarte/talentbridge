package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.ProviderStatisticsRepository;
import com.dunkweb.talentbridge.domain.entity.ProviderStatistics;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderExecutionResult;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderStatisticsServiceTest {

    @Test
    void shouldReturnEmptyProviderWhenThereIsNoExecution() {
        ProviderStatisticsService service = new ProviderStatisticsService(new InMemoryProviderStatisticsRepository());

        ProviderStatistics statistics = service.getByProvider("CSV");

        assertThat(statistics.providerName()).isEqualTo("CSV");
        assertThat(statistics.executions()).isZero();
        assertThat(statistics.averageScore()).isZero();
        assertThat(statistics.lastExecutionAt()).isNull();
    }

    @Test
    void shouldUpdateMetricsForProviderExecution() {
        ProviderStatisticsService service = new ProviderStatisticsService(new InMemoryProviderStatisticsRepository());

        service.updateFromExecution(
                List.of(new ProviderExecutionResult("CSV", 3, 3, 0, 3, 3, 0, 120L, true, List.of(), List.of())),
                List.of(
                        candidate("CSV", "A", 10),
                        candidate("CSV", "B", 20),
                        candidate("CSV", "C", 30)
                ),
                "job-1"
        );

        ProviderStatistics statistics = service.getByProvider("CSV");

        assertThat(statistics.executions()).isEqualTo(1);
        assertThat(statistics.successfulExecutions()).isEqualTo(1);
        assertThat(statistics.failedExecutions()).isZero();
        assertThat(statistics.candidatesFound()).isEqualTo(3);
        assertThat(statistics.candidatesImported()).isEqualTo(3);
        assertThat(statistics.averageExecutionTimeMs()).isEqualTo(120.0);
        assertThat(statistics.averageScore()).isEqualTo(20.0);
        assertThat(statistics.lastExecutionAt()).isNotNull();
    }

    @Test
    void shouldAggregateMultipleProviderExecutionsAndAverageScores() {
        ProviderStatisticsService service = new ProviderStatisticsService(new InMemoryProviderStatisticsRepository());

        service.updateFromExecution(
                List.of(new ProviderExecutionResult("MANUAL", 2, 2, 0, 2, 2, 0, 100L, true, List.of(), List.of())),
                List.of(candidate("MANUAL", "A", 10), candidate("MANUAL", "B", 20)),
                "job-1"
        );

        service.updateFromExecution(
                List.of(new ProviderExecutionResult("MANUAL", 1, 1, 0, 1, 1, 0, 50L, true, List.of(), List.of())),
                List.of(candidate("MANUAL", "C", 30)),
                "job-2"
        );

        ProviderStatistics statistics = service.getByProvider("MANUAL");

        assertThat(statistics.executions()).isEqualTo(2);
        assertThat(statistics.successfulExecutions()).isEqualTo(2);
        assertThat(statistics.candidatesFound()).isEqualTo(3);
        assertThat(statistics.candidatesImported()).isEqualTo(3);
        assertThat(statistics.averageExecutionTimeMs()).isEqualTo(75.0);
        assertThat(statistics.averageScore()).isEqualTo(20.0);
    }

    @Test
    void shouldBuildRankingUsingAverageScoreThenImportedCandidates() {
        ProviderStatisticsService service = new ProviderStatisticsService(new InMemoryProviderStatisticsRepository());

        service.updateFromExecution(
                List.of(new ProviderExecutionResult("CSV", 2, 1, 1, 2, 1, 1, 80L, true, List.of(), List.of())),
                List.of(candidate("CSV", "A", 12), candidate("CSV", "B", 12)),
                "job-csv"
        );

        service.updateFromExecution(
                List.of(new ProviderExecutionResult("MANUAL", 3, 2, 1, 3, 2, 1, 70L, true, List.of(), List.of())),
                List.of(candidate("MANUAL", "C", 18), candidate("MANUAL", "D", 18)),
                "job-manual"
        );

        List<ProviderStatistics> ranking = service.ranking();

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).providerName()).isEqualTo("MANUAL");
        assertThat(ranking.get(1).providerName()).isEqualTo("CSV");
    }

    @Test
    void shouldHandleMultipleProvidersInListAll() {
        ProviderStatisticsService service = new ProviderStatisticsService(new InMemoryProviderStatisticsRepository());

        service.updateFromExecution(
                List.of(new ProviderExecutionResult("CSV", 1, 1, 0, 1, 1, 0, 30L, true, List.of(), List.of())),
                List.of(candidate("CSV", "A", 10)),
                "job-csv"
        );
        service.updateFromExecution(
                List.of(new ProviderExecutionResult("MANUAL", 1, 1, 0, 1, 1, 0, 20L, true, List.of(), List.of())),
                List.of(candidate("MANUAL", "B", 20)),
                "job-manual"
        );

        List<ProviderStatistics> all = service.listAll();

        assertThat(all).hasSize(2);
        assertThat(all.stream().map(ProviderStatistics::providerName)).containsExactly("CSV", "MANUAL");
    }

    private RecruiterCandidate candidate(String provider, String name, int score) {
        return new RecruiterCandidate(
                UUID.randomUUID(),
                provider,
                DiscoverySource.valueOf(provider),
                "MANUAL_ENTRY",
                name,
                "Tech Recruiter",
                "Company",
                "Sao Paulo",
                "Hiring",
                "https://linkedin.com/in/" + name.toLowerCase(),
                1.0,
                Instant.now(),
                Map.of(),
                Map.of("score", score)
        );
    }

    private static class InMemoryProviderStatisticsRepository implements ProviderStatisticsRepository {

        private final Map<String, ProviderStatistics> storage = new HashMap<>();

        @Override
        public ProviderStatistics save(ProviderStatistics statistics) {
            storage.put(statistics.providerName().toUpperCase(), statistics);
            return statistics;
        }

        @Override
        public Optional<ProviderStatistics> findByProviderName(String providerName) {
            if (providerName == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(storage.get(providerName.toUpperCase()));
        }

        @Override
        public List<ProviderStatistics> findAll() {
            return new ArrayList<>(storage.values());
        }
    }
}
