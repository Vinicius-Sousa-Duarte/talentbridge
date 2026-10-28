package com.dunkweb.talentbridge.application.dto;

public record RecruiterDiscoveryResponse(
        String name,
        String position,
        String company,
        String location,
        String linkedinUrl,
        String headline,
        Integer connectionsCount,
        Integer score
) {
}
