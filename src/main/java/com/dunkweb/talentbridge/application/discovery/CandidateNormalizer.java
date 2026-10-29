package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;

public interface CandidateNormalizer {

    RecruiterCandidate normalize(RecruiterCandidate candidate, DiscoveryContext context);
}
