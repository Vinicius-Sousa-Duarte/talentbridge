package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.UpdateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.application.mapper.UserProfileDtoMapper;
import com.dunkweb.talentbridge.application.port.input.UpdateUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateUserProfileUseCaseImpl implements UpdateUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileDtoMapper userProfileDtoMapper;

    @Override
    public UserProfileResponse execute(UpdateUserProfileRequest request) {
        long startedAt = System.nanoTime();
        log.info("{} User profile update started skillsCount={} objectivesCount={}",
            USE_CASE,
            request.skills().size(),
            request.objectives() == null ? 0 : request.objectives().size());

        UserProfile activeProfile = userProfileRepository.findActiveProfile()
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional ativo nao encontrado."));

        activeProfile.update(
                request.professionalTitle(),
                request.professionalSummary(),
                request.objectives(),
                request.skills().stream()
                        .map(skill -> new UserProfile.SkillInput(skill.skill(), skill.weight()))
                        .toList()
        );

        UserProfile saved = userProfileRepository.save(activeProfile);
    long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
    log.info("{} User profile update completed profileId={} durationMs={}",
        USE_CASE,
        saved.getId(),
        durationMs);

        return userProfileDtoMapper.toResponse(saved);
    }
}
