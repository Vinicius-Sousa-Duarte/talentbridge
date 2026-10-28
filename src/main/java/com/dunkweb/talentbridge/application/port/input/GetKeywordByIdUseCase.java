package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.KeywordResponse;

import java.util.UUID;

public interface GetKeywordByIdUseCase {

    KeywordResponse execute(UUID keywordId);
}
