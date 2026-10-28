package com.dunkweb.talentbridge.infrastructure.persistence.mapper;

import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.infrastructure.persistence.entity.RecruiterJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecruiterPersistenceMapper {

    public RecruiterJpaEntity toEntity(Recruiter recruiter) {
        return RecruiterJpaEntity.builder()
                .id(recruiter.getId())
                .name(recruiter.getName())
                .position(recruiter.getPosition())
                .company(recruiter.getCompany())
                .location(recruiter.getLocation())
                .linkedinUrl(recruiter.getLinkedinUrl())
                .headline(recruiter.getHeadline())
                .connectionsCount(recruiter.getConnectionsCount())
                .score(recruiter.getScore())
                .status(recruiter.getStatus())
                .createdAt(recruiter.getCreatedAt())
                .invitationSentAt(recruiter.getInvitationSentAt())
                .connectedAt(recruiter.getConnectedAt())
                .build();
    }

    public Recruiter toDomain(RecruiterJpaEntity entity) {
        return Recruiter.builder()
                .id(entity.getId())
                .name(entity.getName())
                .position(entity.getPosition())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .linkedinUrl(entity.getLinkedinUrl())
                .headline(entity.getHeadline())
                .connectionsCount(entity.getConnectionsCount())
                .score(entity.getScore())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .invitationSentAt(entity.getInvitationSentAt())
                .connectedAt(entity.getConnectedAt())
                .build();
    }
}
