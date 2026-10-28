package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.Keyword;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KeywordRepository {

    Keyword save(Keyword keyword);

    Optional<Keyword> findById(UUID id);

    List<Keyword> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
