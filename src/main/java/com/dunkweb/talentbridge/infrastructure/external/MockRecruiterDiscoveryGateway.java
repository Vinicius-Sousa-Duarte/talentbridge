package com.dunkweb.talentbridge.infrastructure.external;

import com.dunkweb.talentbridge.application.port.output.RecruiterDiscoveryGateway;
import com.dunkweb.talentbridge.application.port.output.DiscoveryExtractionResult;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "talentbridge.discovery", name = "provider", havingValue = "mock")
public class MockRecruiterDiscoveryGateway implements RecruiterDiscoveryGateway {

    @Override
        public DiscoveryExtractionResult discoverRecruiters(UserProfile profile, int desiredProfiles, UUID jobId) {
                List<RecruiterProfile> profiles = List.of(
                new RecruiterProfile(
                        "Camila Oliveira",
                        "Senior Tech Recruiter",
                        "SkyTech",
                        "Sao Paulo, Brazil",
                        "https://www.linkedin.com/in/camila-oliveira-tech",
                        "Hiring Java and AWS backend engineers",
                        500
                ),
                new RecruiterProfile(
                        "John Parker",
                        "Talent Acquisition Specialist",
                        "CloudBridge",
                        "Lisbon, Portugal",
                        "https://www.linkedin.com/in/john-parker-ta",
                        "Talent Acquisition for Cloud and Distributed Systems",
                        800
                )
        );

        return new DiscoveryExtractionResult(profiles, 1, 1);
    }
}
