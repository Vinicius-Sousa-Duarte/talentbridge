package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import com.dunkweb.talentbridge.infrastructure.discovery.playwright.DiscoveryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CooldownValidationService {

    private final DiscoveryJobRepository discoveryJobRepository;
    private final DiscoveryProperties discoveryProperties;
    private final Clock clock;

    @Autowired
    public CooldownValidationService(
            DiscoveryJobRepository discoveryJobRepository,
            DiscoveryProperties discoveryProperties
    ) {
        this(discoveryJobRepository, discoveryProperties, Clock.systemDefaultZone());
    }

    CooldownValidationService(
            DiscoveryJobRepository discoveryJobRepository,
            DiscoveryProperties discoveryProperties,
            Clock clock
    ) {
        this.discoveryJobRepository = discoveryJobRepository;
        this.discoveryProperties = discoveryProperties;
        this.clock = clock;
    }

    public void validate() {
        int cooldownMinutes = Math.max(0, discoveryProperties.cooldownMinutes());
        if (cooldownMinutes == 0) {
            return;
        }

        DiscoveryJob latestJob = discoveryJobRepository.findLatestCompleted().orElse(null);
        if (latestJob == null || latestJob.getCreatedAt() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextAllowedAt = latestJob.getCreatedAt().plusMinutes(cooldownMinutes);
        if (now.isBefore(nextAllowedAt)) {
            throw new BusinessException("Discovery em cooldown. Nova execucao permitida em " + nextAllowedAt + ".");
        }
    }
}
