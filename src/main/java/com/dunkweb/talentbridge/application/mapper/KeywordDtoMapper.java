package com.dunkweb.talentbridge.application.mapper;

import com.dunkweb.talentbridge.application.dto.KeywordResponse;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import org.springframework.stereotype.Component;

@Component
public class KeywordDtoMapper {

    public KeywordResponse toResponse(Keyword keyword) {
        return new KeywordResponse(
                keyword.getId(),
                keyword.getDescription(),
                keyword.getCreatedAt()
        );
    }
}
