package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;

import java.util.UUID;

public interface DismissRecruiterUseCase {

    RecruiterResponse execute(UUID recruiterId);
}
