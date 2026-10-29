package com.dunkweb.talentbridge.infrastructure.persistence.repository;

import com.dunkweb.talentbridge.infrastructure.persistence.entity.RecruiterJpaEntity;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SpringDataRecruiterRepository extends JpaRepository<RecruiterJpaEntity, UUID> {

	boolean existsByLinkedinUrlIgnoreCase(String linkedinUrl);

	boolean existsByNameIgnoreCaseAndCompanyIgnoreCase(String name, String company);

	long countByStatus(RecruiterStatus status);

	List<RecruiterJpaEntity> findByOrderByScoreDescCreatedAtDesc(Pageable pageable);

	@Query("select avg(r.score) from RecruiterJpaEntity r")
	Double averageScore();

	@Query("select max(r.score) from RecruiterJpaEntity r")
	Integer highestScore();

	@Query("select min(r.score) from RecruiterJpaEntity r")
	Integer lowestScore();

	@Query("""
			select r.company as company, count(r) as recruiterCount, avg(r.score) as averageScore
			from RecruiterJpaEntity r
			group by r.company
			order by count(r) desc
			""")
	List<CompanyStatisticsProjection> companyStatistics();

	interface CompanyStatisticsProjection {
		String getCompany();
		Long getRecruiterCount();
		Double getAverageScore();
	}
}
