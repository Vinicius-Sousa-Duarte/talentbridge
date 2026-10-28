package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {

    UserProfile save(UserProfile userProfile);

    Optional<UserProfile> findActiveProfile();

    void deleteById(UUID id);
}
