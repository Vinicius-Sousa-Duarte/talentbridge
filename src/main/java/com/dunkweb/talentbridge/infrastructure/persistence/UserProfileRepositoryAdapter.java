package com.dunkweb.talentbridge.infrastructure.persistence;

import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.infrastructure.persistence.mapper.UserProfilePersistenceMapper;
import com.dunkweb.talentbridge.infrastructure.persistence.repository.SpringDataUserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DOMAIN;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserProfileRepositoryAdapter implements UserProfileRepository {

    private final SpringDataUserProfileRepository repository;
    private final UserProfilePersistenceMapper mapper;

    @Override
    public UserProfile save(UserProfile userProfile) {
        boolean isUpdate = repository.existsById(userProfile.getId());
        UserProfile persisted = mapper.toDomain(repository.save(mapper.toEntity(userProfile)));

        log.info("{} User profile {} profileId={} skillsCount={} objectivesCount={}",
                DOMAIN,
                isUpdate ? "updated" : "saved",
                persisted.getId(),
                persisted.getSkillPreferences().size(),
                persisted.getObjectives().size());

        return persisted;
    }

    @Override
    public Optional<UserProfile> findActiveProfile() {
        return repository.findTopByOrderByUpdatedAtDesc().map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
