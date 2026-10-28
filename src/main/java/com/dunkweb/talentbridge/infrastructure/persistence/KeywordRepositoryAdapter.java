package com.dunkweb.talentbridge.infrastructure.persistence;

import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.infrastructure.persistence.mapper.KeywordPersistenceMapper;
import com.dunkweb.talentbridge.infrastructure.persistence.repository.SpringDataKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class KeywordRepositoryAdapter implements KeywordRepository {

    private final SpringDataKeywordRepository repository;
    private final KeywordPersistenceMapper mapper;

    @Override
    public Keyword save(Keyword keyword) {
        return mapper.toDomain(repository.save(mapper.toEntity(keyword)));
    }

    @Override
    public Optional<Keyword> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Keyword> findAll() {
        return repository.findAllByOrderByCreatedAtAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
