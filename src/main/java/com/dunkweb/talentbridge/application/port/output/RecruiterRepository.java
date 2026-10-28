package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.Recruiter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecruiterRepository {

    Recruiter save(Recruiter recruiter);

    Optional<Recruiter> findById(UUID id);

    Optional<Recruiter> findByLinkedinUrl(String linkedinUrl);

    Optional<Recruiter> findByNameAndCompany(String name, String company);

    List<Recruiter> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
