package com.dunkweb.talentbridge.application.mapper;

import com.dunkweb.talentbridge.application.dto.CreateRecruiterRequest;
import com.dunkweb.talentbridge.application.dto.RecruiterDiscoveryResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import org.springframework.stereotype.Component;

@Component
public class RecruiterDtoMapper {

    public Recruiter toDomain(CreateRecruiterRequest request, Integer score) {
        return Recruiter.create(
                request.name(),
                request.position(),
                request.company(),
                request.location(),
                request.linkedinUrl(),
                request.headline(),
                request.connectionsCount(),
                score
        );
    }

    public RecruiterResponse toResponse(Recruiter recruiter) {
        return new RecruiterResponse(
                recruiter.getId(),
                recruiter.getName(),
                recruiter.getPosition(),
                recruiter.getCompany(),
                recruiter.getLocation(),
                recruiter.getLinkedinUrl(),
                recruiter.getHeadline(),
                recruiter.getConnectionsCount(),
                recruiter.getScore(),
                recruiter.getStatus(),
                recruiter.getCreatedAt(),
                recruiter.getInvitationSentAt(),
                recruiter.getConnectedAt()
        );
    }

    public RecruiterDiscoveryResponse toDiscoveryResponse(RecruiterProfile profile, Integer score) {
        return new RecruiterDiscoveryResponse(
                profile.name(),
                profile.position(),
                profile.company(),
                profile.location(),
                profile.linkedinUrl(),
                profile.headline(),
                profile.connectionsCount(),
                score
        );
    }
}
