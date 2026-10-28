package com.dunkweb.talentbridge.application.mapper;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryJobDtoMapper {

    public DiscoveryJobResponse toResponse(DiscoveryJob job) {
        return new DiscoveryJobResponse(
                job.getId(),
                job.getRequestedProfiles(),
                job.getProfilesFound(),
                job.getProfilesImported(),
                job.getDuplicatesIgnored(),
                job.getPagesVisited(),
                job.getQueriesExecuted(),
                job.getExecutionTimeMs(),
                job.getStatus(),
                job.getErrorMessage()
        );
    }
}
