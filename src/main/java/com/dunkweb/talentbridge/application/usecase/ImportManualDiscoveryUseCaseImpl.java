package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.discovery.DiscoveryOrchestrator;
import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.application.port.input.ImportManualDiscoveryUseCase;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.domain.entity.SkillPreference;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DISCOVERY;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportManualDiscoveryUseCaseImpl implements ImportManualDiscoveryUseCase {

    private final DiscoveryOrchestrator discoveryOrchestrator;
    private final UserProfileRepository userProfileRepository;
    private final KeywordRepository keywordRepository;

    @Override
    public DiscoveryResult execute(CreateManualCandidateRequest request, String correlationId) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ValidationException("Nome do recrutador e obrigatorio para discovery manual.");
        }

        UserProfile activeProfile = userProfileRepository.findActiveProfile().orElse(null);

        DiscoveryContext context = new DiscoveryContext(
                UUID.randomUUID(),
                activeProfile == null ? null : activeProfile.getId(),
                activeProfile == null ? List.of() : activeProfile.getSkillPreferences().stream().map(SkillPreference::getSkill).toList(),
                keywordRepository.findAll().stream().map(Keyword::getDescription).toList(),
                activeProfile == null ? List.of() : activeProfile.getObjectives().stream().map(Enum::name).toList(),
                1,
                1,
                1,
                Duration.ofSeconds(30),
                correlationId,
                Map.of(
                        "discoverySource", DiscoverySource.MANUAL,
                        "manualCandidate", request
                )
        );

        log.info("{} Manual discovery execution started correlationId={} provider={} candidateName={}",
                DISCOVERY,
                context.correlationId(),
                DiscoverySource.MANUAL.name(),
                request.name());

        DiscoveryResult result = discoveryOrchestrator.execute(context);

        log.info("{} Manual discovery completed correlationId={} jobId={} provider={} candidateName={} importedCandidates={}",
                DISCOVERY,
                context.correlationId(),
                result.jobId(),
                DiscoverySource.MANUAL.name(),
                request.name(),
                result.importedCandidates());

        return result;
    }
}
