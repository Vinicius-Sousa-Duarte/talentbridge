package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscoveryJobRepository {

    DiscoveryJob save(DiscoveryJob job);

    Optional<DiscoveryJob> findById(UUID id);

    List<DiscoveryJob> findAllOrderByCreatedAtDesc();

    Optional<DiscoveryJob> findLatest();

    Optional<DiscoveryJob> findLatestCompleted();

    boolean existsByStatus(DiscoveryJobStatus status);

    int sumImportedProfilesSince(LocalDateTime from);
}
