package com.dunkweb.talentbridge.infrastructure.persistence.mapper;

import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.infrastructure.persistence.entity.KeywordJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class KeywordPersistenceMapper {

    public KeywordJpaEntity toEntity(Keyword keyword) {
        return KeywordJpaEntity.builder()
                .id(keyword.getId())
                .description(keyword.getDescription())
                .createdAt(keyword.getCreatedAt())
                .build();
    }

    public Keyword toDomain(KeywordJpaEntity entity) {
        return Keyword.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
