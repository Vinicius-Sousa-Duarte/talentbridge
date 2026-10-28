package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.application.dto.KeywordResponse;

import java.util.List;

public interface ListKeywordsUseCase {

    List<KeywordResponse> execute();
}
