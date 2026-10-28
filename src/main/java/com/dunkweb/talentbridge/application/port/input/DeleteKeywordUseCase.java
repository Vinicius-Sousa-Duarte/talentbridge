package com.dunkweb.talentbridge.application.port.input;

import java.util.UUID;

public interface DeleteKeywordUseCase {

    void execute(UUID keywordId);
}
