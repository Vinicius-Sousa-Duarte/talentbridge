package com.dunkweb.talentbridge.infrastructure.persistence;

import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.RecruiterCompanyStatistics;
import com.dunkweb.talentbridge.application.port.output.RecruiterPage;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import com.dunkweb.talentbridge.infrastructure.persistence.mapper.RecruiterPersistenceMapper;
import com.dunkweb.talentbridge.infrastructure.persistence.repository.SpringDataRecruiterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    public RecruiterPage findPage(int page, int size, String sort) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        Sort safeSort = parseSort(sort);

        Page<Recruiter> recruiterPage = repository
                .findAll(PageRequest.of(safePage, safeSize, safeSort))
                .map(mapper::toDomain);

        String appliedSort = safeSort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .reduce((left, right) -> left + ";" + right)
                .orElse("score,desc;createdAt,desc");

        return new RecruiterPage(
                recruiterPage.getContent(),
                recruiterPage.getNumber(),
                recruiterPage.getSize(),
                recruiterPage.getTotalElements(),
                recruiterPage.getTotalPages(),
                appliedSort
        );
    }

    @Override
    public List<Recruiter> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByStatus(RecruiterStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    public double averageScore() {
        Double value = repository.averageScore();
        return value == null ? 0.0 : value;
    }

    @Override
    public int highestScore() {
        Integer value = repository.highestScore();
        return value == null ? 0 : value;
    }

    @Override
    public int lowestScore() {
        Integer value = repository.lowestScore();
        return value == null ? 0 : value;
    }

    @Override
    public List<Recruiter> findTopByScore(int limit) {
        int safeLimit = Math.max(1, limit);
        return repository.findByOrderByScoreDescCreatedAtDesc(PageRequest.of(0, safeLimit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RecruiterCompanyStatistics> companyStatistics() {
        return repository.companyStatistics().stream()
                .map(row -> new RecruiterCompanyStatistics(
                        row.getCompany(),
                        row.getRecruiterCount() == null ? 0 : row.getRecruiterCount(),
                        row.getAverageScore() == null ? 0.0 : row.getAverageScore()
                ))
                .toList();
    }

    @Override
    public boolean existsByLinkedinUrl(String linkedinUrl) {
        if (linkedinUrl == null || linkedinUrl.isBlank()) {
            return false;
        }
        return repository.existsByLinkedinUrlIgnoreCase(linkedinUrl.trim());
    }

    @Override
    public boolean existsByNameAndCompany(String name, String company) {
        if (name == null || name.isBlank() || company == null || company.isBlank()) {
            return false;
        }
        return repository.existsByNameIgnoreCaseAndCompanyIgnoreCase(name.trim(), company.trim());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("score"), Sort.Order.desc("createdAt"));
        }

        List<Sort.Order> orders = new ArrayList<>();
        String[] chunks = sort.split(";");
        for (String chunk : chunks) {
            String[] parts = chunk.split(",");
            if (parts.length == 0 || parts[0].isBlank()) {
                continue;
            }

            String property = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            orders.add(new Sort.Order(sortDirection, property));
        }

        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.desc("score"), Sort.Order.desc("createdAt"));
        }

        return Sort.by(orders);
    }
}
