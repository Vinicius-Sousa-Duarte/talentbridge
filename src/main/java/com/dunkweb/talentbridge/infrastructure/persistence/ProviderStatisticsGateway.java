package com.dunkweb.talentbridge.infrastructure.persistence;

import com.dunkweb.talentbridge.application.port.output.ProviderStatisticsRepository;
import com.dunkweb.talentbridge.domain.entity.ProviderStatistics;
import com.dunkweb.talentbridge.infrastructure.persistence.entity.ProviderStatisticsJpaEntity;
import com.dunkweb.talentbridge.infrastructure.persistence.repository.SpringDataProviderStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProviderStatisticsGateway implements ProviderStatisticsRepository {

    private final SpringDataProviderStatisticsRepository repository;

    @Override
    public ProviderStatistics save(ProviderStatistics statistics) {
        ProviderStatisticsJpaEntity saved = repository.save(toEntity(statistics));
        return toDomain(saved);
    }

    @Override
    public Optional<ProviderStatistics> findByProviderName(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return Optional.empty();
        }
        return repository.findByProviderNameIgnoreCase(providerName.trim()).map(this::toDomain);
    }

    @Override
    public List<ProviderStatistics> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private ProviderStatisticsJpaEntity toEntity(ProviderStatistics statistics) {
        return ProviderStatisticsJpaEntity.builder()
                .id(statistics.id())
                .providerName(statistics.providerName())
                .executions(statistics.executions())
                .successfulExecutions(statistics.successfulExecutions())
                .failedExecutions(statistics.failedExecutions())
                .candidatesFound(statistics.candidatesFound())
                .candidatesImported(statistics.candidatesImported())
                .duplicatesIgnored(statistics.duplicatesIgnored())
                .totalExecutionTimeMs(statistics.totalExecutionTimeMs())
                .totalScore(statistics.totalScore())
                .scoreSamples(statistics.scoreSamples())
                .averageExecutionTimeMs(statistics.averageExecutionTimeMs())
                .averageScore(statistics.averageScore())
                .lastExecutionAt(statistics.lastExecutionAt())
                .build();
    }

    private ProviderStatistics toDomain(ProviderStatisticsJpaEntity entity) {
        return new ProviderStatistics(
                entity.getId(),
                entity.getProviderName(),
                entity.getExecutions(),
                entity.getSuccessfulExecutions(),
                entity.getFailedExecutions(),
                entity.getCandidatesFound(),
                entity.getCandidatesImported(),
                entity.getDuplicatesIgnored(),
                entity.getTotalExecutionTimeMs(),
                entity.getTotalScore(),
                entity.getScoreSamples(),
                entity.getAverageExecutionTimeMs(),
                entity.getAverageScore(),
                entity.getLastExecutionAt()
        );
    }
}
