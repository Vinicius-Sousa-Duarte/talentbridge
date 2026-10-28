package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;

import java.util.List;

public interface ListRecruitersUseCase {

    List<RecruiterResponse> execute();
}
