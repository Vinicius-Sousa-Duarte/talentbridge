package com.dunkweb.talentbridge.infrastructure.persistence.repository;

import com.dunkweb.talentbridge.infrastructure.persistence.entity.ProviderStatisticsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProviderStatisticsRepository extends JpaRepository<ProviderStatisticsJpaEntity, UUID> {

    Optional<ProviderStatisticsJpaEntity> findByProviderNameIgnoreCase(String providerName);
}
