package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.KeywordResponse;
import com.dunkweb.talentbridge.application.mapper.KeywordDtoMapper;
import com.dunkweb.talentbridge.application.port.input.ListKeywordsUseCase;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListKeywordsUseCaseImpl implements ListKeywordsUseCase {

    private final KeywordRepository keywordRepository;
    private final KeywordDtoMapper keywordDtoMapper;

    @Override
    public List<KeywordResponse> execute() {
        return keywordRepository.findAll().stream()
                .map(keywordDtoMapper::toResponse)
                .toList();
    }
}
