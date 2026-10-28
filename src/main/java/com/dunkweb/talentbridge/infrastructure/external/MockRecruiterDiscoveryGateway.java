package com.dunkweb.talentbridge.infrastructure.external;

import com.dunkweb.talentbridge.application.port.output.RecruiterDiscoveryGateway;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockRecruiterDiscoveryGateway implements RecruiterDiscoveryGateway {

    @Override
    public List<RecruiterProfile> discoverRecruiters() {
        return List.of(
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
    }
}
