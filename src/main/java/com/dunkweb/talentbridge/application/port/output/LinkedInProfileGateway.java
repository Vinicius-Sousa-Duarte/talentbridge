package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;

import java.util.Optional;

public interface LinkedInProfileGateway {

    Optional<RecruiterProfile> findByProfileUrl(String linkedinUrl);
}
