package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterDiscoveryResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.SearchRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterDiscoveryGateway;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchRecruitersUseCaseImpl implements SearchRecruitersUseCase {

    private final RecruiterDiscoveryGateway recruiterDiscoveryGateway;
    private final RecruiterDtoMapper recruiterDtoMapper;
    private final UserProfileRepository userProfileRepository;
    private final RecruiterScoringEngine recruiterScoringEngine;

    @Override
    public List<RecruiterDiscoveryResponse> execute() {
        long startedAt = System.nanoTime();
        UserProfile activeProfile = userProfileRepository.findActiveProfile().orElse(null);

        log.info("{} Recruiter discovery started activeProfilePresent={}",
            USE_CASE,
            activeProfile != null);

        List<RecruiterDiscoveryResponse> discovered = recruiterDiscoveryGateway.discoverRecruiters()
                .stream()
                .map(profile -> {
                int score = recruiterScoringEngine.calculate(activeProfile, profile.headline(), profile.position(), null);
                    return recruiterDtoMapper.toDiscoveryResponse(profile, score);
                })
                .toList();

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} Recruiter discovery completed discoveredCount={} durationMs={}",
            USE_CASE,
            discovered.size(),
            durationMs);

        return discovered;
    }
}
