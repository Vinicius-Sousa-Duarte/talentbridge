package com.dunkweb.talentbridge.domain.valueobject;

public record RecruiterProfile(
        String name,
        String position,
        String company,
        String location,
        String linkedinUrl,
        String headline,
        Integer connectionsCount
) {
}
