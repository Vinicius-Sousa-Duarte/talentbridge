package com.dunkweb.talentbridge.infrastructure.persistence;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
import com.dunkweb.talentbridge.infrastructure.persistence.mapper.DiscoveryJobPersistenceMapper;
import com.dunkweb.talentbridge.infrastructure.persistence.repository.SpringDataDiscoveryJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DiscoveryJobRepositoryAdapter implements DiscoveryJobRepository {

    private final SpringDataDiscoveryJobRepository repository;
    private final DiscoveryJobPersistenceMapper mapper;

    @Override
    public DiscoveryJob save(DiscoveryJob job) {
        return mapper.toDomain(repository.save(mapper.toEntity(job)));
    }

    @Override
    public Optional<DiscoveryJob> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<DiscoveryJob> findAllOrderByCreatedAtDesc() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<DiscoveryJob> findLatest() {
        return repository.findTopByOrderByCreatedAtDesc().map(mapper::toDomain);
    }

    @Override
    public Optional<DiscoveryJob> findLatestCompleted() {
        return repository.findTopByStatusOrderByCreatedAtDesc(DiscoveryJobStatus.COMPLETED).map(mapper::toDomain);
    }

    @Override
    public boolean existsByStatus(DiscoveryJobStatus status) {
        return repository.existsByStatus(status);
    }

    @Override
    public int sumImportedProfilesSince(LocalDateTime from) {
        Long result = repository.sumImportedProfilesSince(from);
        return result == null ? 0 : result.intValue();
    }
}
