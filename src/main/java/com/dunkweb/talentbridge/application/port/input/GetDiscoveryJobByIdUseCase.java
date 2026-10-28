package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;

import java.util.UUID;

public interface GetDiscoveryJobByIdUseCase {

    DiscoveryJobResponse execute(UUID jobId);
}
