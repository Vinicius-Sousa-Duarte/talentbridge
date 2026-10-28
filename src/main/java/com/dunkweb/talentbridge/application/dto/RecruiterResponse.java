package com.dunkweb.talentbridge.application.dto;

import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecruiterResponse(
        UUID id,
        String name,
        String position,
        String company,
        String location,
        String linkedinUrl,
        String headline,
        Integer connectionsCount,
        Integer score,
        RecruiterStatus status,
        LocalDateTime createdAt,
        LocalDateTime invitationSentAt,
        LocalDateTime connectedAt
) {
}
