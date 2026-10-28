package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.application.dto.ExecuteRecruiterDiscoveryRequest;

public interface ExecuteRecruiterDiscoveryUseCase {

    DiscoveryJobResponse execute(ExecuteRecruiterDiscoveryRequest request);
}
