package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;

public interface ImportManualDiscoveryUseCase {

    DiscoveryResult execute(CreateManualCandidateRequest request, String correlationId);
}
