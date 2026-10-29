package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterDiscoveryResponse;
import com.dunkweb.talentbridge.application.discovery.DiscoveryOrchestrator;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.SearchRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.domain.entity.SkillPreference;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchRecruitersUseCaseImpl implements SearchRecruitersUseCase {

    private final DiscoveryOrchestrator discoveryOrchestrator;
    private final RecruiterDtoMapper recruiterDtoMapper;
    private final UserProfileRepository userProfileRepository;
    private final KeywordRepository keywordRepository;

    @Override
    public List<RecruiterDiscoveryResponse> execute() {
        long startedAt = System.nanoTime();
        UserProfile activeProfile = userProfileRepository.findActiveProfile().orElse(null);

        log.info("{} Recruiter discovery started activeProfilePresent={}",
            USE_CASE,
            activeProfile != null);

        DiscoveryContext context = new DiscoveryContext(
            UUID.randomUUID(),
            activeProfile == null ? null : activeProfile.getId(),
            activeProfile == null ? List.of() : activeProfile.getSkillPreferences().stream().map(SkillPreference::getSkill).toList(),
            keywordRepository.findAll().stream().map(Keyword::getDescription).toList(),
            activeProfile == null ? List.of() : activeProfile.getObjectives().stream().map(Enum::name).toList(),
            30,
            100,
            20,
            Duration.ofSeconds(30),
            MDC.get(REQUEST_ID),
            Map.of("entrypoint", "SearchRecruitersUseCase")
        );

        DiscoveryResult discoveryResult = discoveryOrchestrator.execute(context);

        List<RecruiterDiscoveryResponse> discovered = discoveryResult.candidates().stream()
            .map(recruiterDtoMapper::toDiscoveryResponse)
                .toList();

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} Recruiter discovery completed discoveredCount={} durationMs={}",
            USE_CASE,
            discovered.size(),
            durationMs);

        return discovered;
    }
}
