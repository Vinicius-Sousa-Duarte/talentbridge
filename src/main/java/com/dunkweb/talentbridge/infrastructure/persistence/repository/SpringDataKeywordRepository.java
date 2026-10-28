package com.dunkweb.talentbridge.infrastructure.persistence.repository;

import com.dunkweb.talentbridge.infrastructure.persistence.entity.KeywordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataKeywordRepository extends JpaRepository<KeywordJpaEntity, UUID> {
}
