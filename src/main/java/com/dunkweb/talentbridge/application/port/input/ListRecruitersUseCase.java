package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.RecruiterPageResponse;

public interface ListRecruitersUseCase {

    RecruiterPageResponse execute(Integer page, Integer size, String sort);
}
