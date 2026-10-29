package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.discovery.DiscoveryOrchestrator;
import com.dunkweb.talentbridge.application.port.input.ImportCsvDiscoveryUseCase;
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
public class ImportCsvDiscoveryUseCaseImpl implements ImportCsvDiscoveryUseCase {

    private final DiscoveryOrchestrator discoveryOrchestrator;
    private final UserProfileRepository userProfileRepository;
    private final KeywordRepository keywordRepository;

    @Override
    public DiscoveryResult execute(String csvContent, Integer desiredProfiles, String correlationId) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new ValidationException("Arquivo CSV e obrigatorio.");
        }

        UserProfile activeProfile = userProfileRepository.findActiveProfile().orElse(null);
        int safeDesiredProfiles = desiredProfiles == null ? 100 : Math.max(1, desiredProfiles);

        DiscoveryContext context = new DiscoveryContext(
                UUID.randomUUID(),
                activeProfile == null ? null : activeProfile.getId(),
                activeProfile == null ? List.of() : activeProfile.getSkillPreferences().stream().map(SkillPreference::getSkill).toList(),
                keywordRepository.findAll().stream().map(Keyword::getDescription).toList(),
                activeProfile == null ? List.of() : activeProfile.getObjectives().stream().map(Enum::name).toList(),
                safeDesiredProfiles,
                safeDesiredProfiles,
                1,
                Duration.ofSeconds(30),
                correlationId,
                Map.of(
                        "discoverySource", DiscoverySource.CSV,
                        "csvContent", csvContent
                )
        );

        log.info("{} CSV discovery execution started correlationId={} desiredProfiles={}",
                DISCOVERY,
                context.correlationId(),
                safeDesiredProfiles);

        return discoveryOrchestrator.execute(context);
    }
}
