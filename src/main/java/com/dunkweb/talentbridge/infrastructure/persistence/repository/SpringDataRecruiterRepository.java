package com.dunkweb.talentbridge.infrastructure.persistence.repository;

import com.dunkweb.talentbridge.infrastructure.persistence.entity.RecruiterJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataRecruiterRepository extends JpaRepository<RecruiterJpaEntity, UUID> {

	Optional<RecruiterJpaEntity> findFirstByLinkedinUrlIgnoreCase(String linkedinUrl);

	Optional<RecruiterJpaEntity> findFirstByNameIgnoreCaseAndCompanyIgnoreCase(String name, String company);
}
