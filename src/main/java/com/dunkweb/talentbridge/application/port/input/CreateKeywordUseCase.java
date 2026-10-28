package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.CreateKeywordRequest;
import com.dunkweb.talentbridge.application.dto.KeywordResponse;

public interface CreateKeywordUseCase {

    KeywordResponse execute(CreateKeywordRequest request);
}
