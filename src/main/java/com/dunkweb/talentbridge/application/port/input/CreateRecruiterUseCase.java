package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.CreateRecruiterRequest;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;

public interface CreateRecruiterUseCase {

    RecruiterResponse execute(CreateRecruiterRequest request);
}
