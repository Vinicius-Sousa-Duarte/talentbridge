package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.RecruiterDiscoveryResponse;

import java.util.List;

public interface SearchRecruitersUseCase {

    List<RecruiterDiscoveryResponse> execute();
}
