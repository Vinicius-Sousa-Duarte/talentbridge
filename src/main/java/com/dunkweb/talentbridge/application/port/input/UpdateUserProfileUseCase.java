package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.UpdateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;

public interface UpdateUserProfileUseCase {

    UserProfileResponse execute(UpdateUserProfileRequest request);
}
