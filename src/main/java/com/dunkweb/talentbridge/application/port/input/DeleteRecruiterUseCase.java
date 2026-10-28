package com.dunkweb.talentbridge.application.port.input;

import java.util.UUID;

public interface DeleteRecruiterUseCase {

    void execute(UUID recruiterId);
}
