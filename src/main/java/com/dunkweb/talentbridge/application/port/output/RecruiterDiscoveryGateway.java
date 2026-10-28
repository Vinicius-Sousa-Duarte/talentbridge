package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.UserProfile;
import java.util.UUID;

public interface RecruiterDiscoveryGateway {

    DiscoveryExtractionResult discoverRecruiters(UserProfile profile, int desiredProfiles, UUID jobId);
}
