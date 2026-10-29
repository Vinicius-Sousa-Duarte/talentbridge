package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;

import java.util.List;

public interface CandidateDeduplicationService {

    CandidateDeduplicationResult deduplicate(List<RecruiterCandidate> candidates);
}
