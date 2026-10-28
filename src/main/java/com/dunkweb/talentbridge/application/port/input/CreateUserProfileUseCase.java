package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.CreateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;

public interface CreateUserProfileUseCase {

    UserProfileResponse execute(CreateUserProfileRequest request);
}
