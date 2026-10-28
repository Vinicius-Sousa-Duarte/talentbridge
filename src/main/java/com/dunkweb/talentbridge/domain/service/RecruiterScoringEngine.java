package com.dunkweb.talentbridge.domain.service;

import com.dunkweb.talentbridge.domain.entity.UserProfile;

import java.util.UUID;

public interface RecruiterScoringEngine {

    default int calculate(UserProfile userProfile, String headline, String description) {
        return calculate(userProfile, headline, description, null);
    }

    int calculate(UserProfile userProfile, String headline, String description, UUID recruiterId);
}
