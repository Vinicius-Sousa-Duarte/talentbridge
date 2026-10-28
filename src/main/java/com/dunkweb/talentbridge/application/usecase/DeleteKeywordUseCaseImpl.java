package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.port.input.DeleteKeywordUseCase;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteKeywordUseCaseImpl implements DeleteKeywordUseCase {

    private final KeywordRepository keywordRepository;

    @Override
    public void execute(UUID keywordId) {
        if (!keywordRepository.existsById(keywordId)) {
            throw new ResourceNotFoundException("Keyword nao encontrada para o id: " + keywordId);
        }
        keywordRepository.deleteById(keywordId);
    }
}
