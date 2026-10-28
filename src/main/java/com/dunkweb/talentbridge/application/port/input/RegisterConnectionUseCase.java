package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;

import java.util.UUID;

public interface RegisterConnectionUseCase {

    RecruiterResponse execute(UUID recruiterId);
}
