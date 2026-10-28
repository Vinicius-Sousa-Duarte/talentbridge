package com.dunkweb.talentbridge.infrastructure.persistence.repository;

import com.dunkweb.talentbridge.infrastructure.persistence.entity.DiscoveryJobJpaEntity;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataDiscoveryJobRepository extends JpaRepository<DiscoveryJobJpaEntity, UUID> {

    List<DiscoveryJobJpaEntity> findAllByOrderByCreatedAtDesc();

    Optional<DiscoveryJobJpaEntity> findTopByOrderByCreatedAtDesc();

    Optional<DiscoveryJobJpaEntity> findTopByStatusOrderByCreatedAtDesc(DiscoveryJobStatus status);

    boolean existsByStatus(DiscoveryJobStatus status);

    @Query("select coalesce(sum(d.profilesImported), 0) from DiscoveryJobJpaEntity d where d.createdAt >= :from")
    Long sumImportedProfilesSince(@Param("from") LocalDateTime from);
}
