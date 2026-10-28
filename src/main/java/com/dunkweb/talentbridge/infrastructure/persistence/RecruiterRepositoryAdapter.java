package com.dunkweb.talentbridge.infrastructure.persistence;

import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.infrastructure.persistence.mapper.RecruiterPersistenceMapper;
import com.dunkweb.talentbridge.infrastructure.persistence.repository.SpringDataRecruiterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DOMAIN;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RecruiterRepositoryAdapter implements RecruiterRepository {

    private final SpringDataRecruiterRepository repository;
    private final RecruiterPersistenceMapper mapper;

    @Override
    public Recruiter save(Recruiter recruiter) {
        boolean isUpdate = repository.existsById(recruiter.getId());
        Recruiter persisted = mapper.toDomain(repository.save(mapper.toEntity(recruiter)));

        log.info("{} Recruiter {} recruiterId={} status={} score={}",
                DOMAIN,
                isUpdate ? "updated" : "saved",
                persisted.getId(),
                persisted.getStatus(),
                persisted.getScore());

        return persisted;
    }

    @Override
    public Optional<Recruiter> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Recruiter> findByLinkedinUrl(String linkedinUrl) {
        if (linkedinUrl == null || linkedinUrl.isBlank()) {
            return Optional.empty();
        }
        return repository.findFirstByLinkedinUrlIgnoreCase(linkedinUrl).map(mapper::toDomain);
    }

    @Override
    public Optional<Recruiter> findByNameAndCompany(String name, String company) {
        if (name == null || name.isBlank() || company == null || company.isBlank()) {
            return Optional.empty();
        }
        return repository.findFirstByNameIgnoreCaseAndCompanyIgnoreCase(name, company).map(mapper::toDomain);
    }

    @Override
    public List<Recruiter> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
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
