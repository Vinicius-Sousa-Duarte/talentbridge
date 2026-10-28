package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;

import java.util.List;

public interface RecruiterDiscoveryGateway {

    List<RecruiterProfile> discoverRecruiters();
}
