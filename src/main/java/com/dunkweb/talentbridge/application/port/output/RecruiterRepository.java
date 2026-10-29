package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecruiterRepository {

    Recruiter save(Recruiter recruiter);

    Optional<Recruiter> findById(UUID id);

    RecruiterPage findPage(int page, int size, String sort);

    List<Recruiter> findAll();

    long count();

    long countByStatus(RecruiterStatus status);

    double averageScore();

    int highestScore();

    int lowestScore();

    List<Recruiter> findTopByScore(int limit);

    List<RecruiterCompanyStatistics> companyStatistics();

    boolean existsByLinkedinUrl(String linkedinUrl);

    boolean existsByNameAndCompany(String name, String company);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
