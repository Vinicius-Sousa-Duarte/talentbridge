package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.CreateKeywordRequest;
import com.dunkweb.talentbridge.application.dto.KeywordResponse;
import com.dunkweb.talentbridge.application.mapper.KeywordDtoMapper;
import com.dunkweb.talentbridge.application.port.input.CreateKeywordUseCase;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateKeywordUseCaseImpl implements CreateKeywordUseCase {

    private final KeywordRepository keywordRepository;
    private final KeywordDtoMapper keywordDtoMapper;

    @Override
    public KeywordResponse execute(CreateKeywordRequest request) {
        Keyword keyword = Keyword.create(request.description());
        Keyword savedKeyword = keywordRepository.save(keyword);
        return keywordDtoMapper.toResponse(savedKeyword);
    }
}
