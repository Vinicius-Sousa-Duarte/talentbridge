package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.CreateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.application.mapper.UserProfileDtoMapper;
import com.dunkweb.talentbridge.application.port.input.CreateUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateUserProfileUseCaseImpl implements CreateUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileDtoMapper userProfileDtoMapper;

    @Override
    public UserProfileResponse execute(CreateUserProfileRequest request) {
        long startedAt = System.nanoTime();
        log.info("{} User profile creation started skillsCount={} objectivesCount={}",
                USE_CASE,
                request.skills().size(),
                request.objectives() == null ? 0 : request.objectives().size());

        if (userProfileRepository.findActiveProfile().isPresent()) {
            throw new BusinessException("Ja existe um perfil profissional ativo. Use atualizacao para alterar preferencias.");
        }

        UserProfile userProfile = UserProfile.create(
                request.professionalTitle(),
                request.professionalSummary(),
                request.objectives(),
                request.skills().stream()
                        .map(skill -> new UserProfile.SkillInput(skill.skill(), skill.weight()))
                        .toList()
        );

        UserProfile savedProfile = userProfileRepository.save(userProfile);
    long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
    log.info("{} User profile creation completed profileId={} durationMs={}",
        USE_CASE,
        savedProfile.getId(),
        durationMs);

        return userProfileDtoMapper.toResponse(savedProfile);
    }
}
