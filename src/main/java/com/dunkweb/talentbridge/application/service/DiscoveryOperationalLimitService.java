package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import com.dunkweb.talentbridge.infrastructure.discovery.playwright.DiscoveryProperties;
import com.dunkweb.talentbridge.shared.constants.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscoveryOperationalLimitService {

    private static final int MIN_ALLOWED = 1;

    private final DiscoveryJobRepository discoveryJobRepository;
    private final DiscoveryProperties discoveryProperties;

    public int resolveDesiredProfiles(int requestedProfiles, String jobIdForLogs) {
        int perExecutionLimit = Math.max(MIN_ALLOWED, discoveryProperties.maxProfilesPerExecution());
        int perDayLimit = Math.max(perExecutionLimit, discoveryProperties.maxProfilesPerDay());

        int importedToday = discoveryJobRepository.sumImportedProfilesSince(LocalDate.now().atStartOfDay());
        int remainingToday = perDayLimit - Math.max(0, importedToday);
        if (remainingToday <= 0) {
            throw new BusinessException("Limite diario de perfis atingido para discovery.");
        }

        int cappedByExecution = Math.min(requestedProfiles, perExecutionLimit);
        if (cappedByExecution < requestedProfiles) {
            log.warn("{} Execution limit applied jobId={} requestedProfiles={} maxProfilesPerExecution={}",
                    LogConstants.DISCOVERY,
                    jobIdForLogs,
                    requestedProfiles,
                    perExecutionLimit);
        }

        int finalDesired = Math.min(cappedByExecution, remainingToday);
        if (finalDesired < cappedByExecution) {
            log.warn("{} Daily limit applied jobId={} requestedProfiles={} remainingToday={} maxProfilesPerDay={} importedToday={}",
                    LogConstants.DISCOVERY,
                    jobIdForLogs,
                    requestedProfiles,
                    remainingToday,
                    perDayLimit,
                    importedToday);
        }

        return Math.max(MIN_ALLOWED, finalDesired);
    }
}
