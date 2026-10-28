package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.KeywordResponse;
import com.dunkweb.talentbridge.application.mapper.KeywordDtoMapper;
import com.dunkweb.talentbridge.application.port.input.GetKeywordByIdUseCase;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetKeywordByIdUseCaseImpl implements GetKeywordByIdUseCase {

    private final KeywordRepository keywordRepository;
    private final KeywordDtoMapper keywordDtoMapper;

    @Override
    public KeywordResponse execute(UUID keywordId) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword nao encontrada para o id: " + keywordId));

        return keywordDtoMapper.toResponse(keyword);
    }
}
