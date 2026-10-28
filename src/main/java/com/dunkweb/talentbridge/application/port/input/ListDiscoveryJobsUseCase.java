package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;

import java.util.List;

public interface ListDiscoveryJobsUseCase {

    List<DiscoveryJobResponse> execute();
}
