package com.dunkweb.talentbridge.infrastructure.persistence.mapper;

import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import com.dunkweb.talentbridge.infrastructure.persistence.entity.DiscoveryJobJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryJobPersistenceMapper {

    public DiscoveryJobJpaEntity toEntity(DiscoveryJob job) {
        return DiscoveryJobJpaEntity.builder()
                .id(job.getId())
                .requestedProfiles(job.getRequestedProfiles())
                .profilesFound(job.getProfilesFound())
                .profilesImported(job.getProfilesImported())
                .duplicatesIgnored(job.getDuplicatesIgnored())
                .pagesVisited(job.getPagesVisited())
                .queriesExecuted(job.getQueriesExecuted())
                .executionTimeMs(job.getExecutionTimeMs())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }

    public DiscoveryJob toDomain(DiscoveryJobJpaEntity entity) {
        return DiscoveryJob.restore(
                entity.getId(),
                entity.getRequestedProfiles(),
                entity.getProfilesFound(),
                entity.getProfilesImported(),
                entity.getDuplicatesIgnored(),
                entity.getPagesVisited(),
                entity.getQueriesExecuted(),
                entity.getExecutionTimeMs(),
                entity.getStatus(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getFinishedAt()
        );
    }
}
