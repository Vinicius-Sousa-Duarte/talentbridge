package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.application.dto.ExecuteRecruiterDiscoveryRequest;
import com.dunkweb.talentbridge.application.mapper.DiscoveryJobDtoMapper;
import com.dunkweb.talentbridge.application.port.output.DiscoveryExtractionResult;
import com.dunkweb.talentbridge.application.port.input.ExecuteRecruiterDiscoveryUseCase;
import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.application.port.output.RecruiterDiscoveryGateway;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.application.service.CooldownValidationService;
import com.dunkweb.talentbridge.application.service.DiscoveryOperationalLimitService;
import com.dunkweb.talentbridge.application.service.RecruiterDeduplicationService;
import com.dunkweb.talentbridge.application.service.SingleDiscoveryJobGuard;
import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import com.dunkweb.talentbridge.infrastructure.discovery.playwright.DiscoveryExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DISCOVERY;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.DUPLICATE;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.ERROR;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROFILE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecuteRecruiterDiscoveryUseCaseImpl implements ExecuteRecruiterDiscoveryUseCase {

    private static final int DEFAULT_DESIRED_PROFILES = 100;

    private final DiscoveryJobRepository discoveryJobRepository;
    private final UserProfileRepository userProfileRepository;
    private final RecruiterDiscoveryGateway recruiterDiscoveryGateway;
    private final RecruiterDeduplicationService recruiterDeduplicationService;
    private final RecruiterScoringEngine recruiterScoringEngine;
    private final RecruiterRepository recruiterRepository;
    private final DiscoveryJobDtoMapper discoveryJobDtoMapper;
    private final SingleDiscoveryJobGuard singleDiscoveryJobGuard;
    private final CooldownValidationService cooldownValidationService;
    private final DiscoveryOperationalLimitService discoveryOperationalLimitService;

    @Override
    public DiscoveryJobResponse execute(ExecuteRecruiterDiscoveryRequest request) {
        int requestedProfiles = request.desiredProfiles() == null ? DEFAULT_DESIRED_PROFILES : request.desiredProfiles();
        long startedAt = System.nanoTime();

        DiscoveryJob job = singleDiscoveryJobGuard.runWithLock(() -> {
            singleDiscoveryJobGuard.ensureNoRunningJob();
            cooldownValidationService.validate();

            int effectiveDesiredProfiles = discoveryOperationalLimitService.resolveDesiredProfiles(requestedProfiles, "N/A");
            DiscoveryJob runningJob = discoveryJobRepository.save(DiscoveryJob.start(effectiveDesiredProfiles));

            log.info("{} Job started jobId={} requestedProfiles={} effectiveDesiredProfiles={}",
                DISCOVERY,
                runningJob.getId(),
                requestedProfiles,
                effectiveDesiredProfiles);
            return runningJob;
        });

        int desiredProfiles = job.getRequestedProfiles();

        try {
            UserProfile activeProfile = userProfileRepository.findActiveProfile()
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional ativo nao encontrado."));

            DiscoveryExtractionResult extractionResult = recruiterDiscoveryGateway.discoverRecruiters(
                    activeProfile,
                    desiredProfiles,
                    job.getId()
            );
            List<RecruiterProfile> extractedProfiles = extractionResult.profiles();

            int profilesFound = extractedProfiles.size();
            int profilesImported = 0;
            int duplicatesIgnored = 0;

            Set<String> sessionLinkedinUrls = new HashSet<>();
            Set<String> sessionNameCompanyKeys = new HashSet<>();

            for (RecruiterProfile profile : extractedProfiles) {
                if (profilesImported >= desiredProfiles) {
                    break;
                }

                try {
                    if (recruiterDeduplicationService.isDuplicate(profile, sessionLinkedinUrls, sessionNameCompanyKeys)) {
                        duplicatesIgnored++;
                        log.debug("{} Duplicate recruiter detected jobId={} linkedinUrl={} name={} company={}",
                                DUPLICATE,
                                job.getId(),
                                profile.linkedinUrl(),
                                profile.name(),
                                profile.company());
                        continue;
                    }

                    if (profile.name() == null || profile.name().isBlank()) {
                        log.debug("{} Recruiter ignored due to missing name jobId={} linkedinUrl={}",
                                DISCOVERY,
                                job.getId(),
                                profile.linkedinUrl());
                        continue;
                    }

                    int score = recruiterScoringEngine.calculate(
                            activeProfile,
                            profile.headline(),
                            safe(profile.position()) + " " + safe(profile.company()),
                            null
                    );

                    Recruiter recruiter = Recruiter.create(
                            profile.name(),
                            profile.position(),
                            profile.company(),
                            profile.location(),
                            profile.linkedinUrl(),
                            profile.headline(),
                            profile.connectionsCount(),
                            score
                    );

                    recruiterRepository.save(recruiter);
                    profilesImported++;
                    log.debug("{} Recruiter imported jobId={} recruiterName={} score={}",
                            PROFILE,
                            job.getId(),
                            recruiter.getName(),
                            recruiter.getScore());
                } catch (Exception profileException) {
                    log.warn("{} Failed to import recruiter profile jobId={} recruiterName={} message={}",
                            DISCOVERY,
                            job.getId(),
                            profile.name(),
                            profileException.getMessage());
                }
            }

            long executionTimeMs = (System.nanoTime() - startedAt) / 1_000_000;
                job.complete(
                    profilesFound,
                    profilesImported,
                    duplicatesIgnored,
                    extractionResult.pagesVisited(),
                    extractionResult.queriesExecuted(),
                    executionTimeMs
                );
            DiscoveryJob completedJob = discoveryJobRepository.save(job);

                log.info("{} Job completed jobId={} profilesFound={} profilesImported={} duplicatesIgnored={} queriesExecuted={} pagesVisited={} executionTimeMs={}",
                    DISCOVERY,
                    completedJob.getId(),
                    completedJob.getProfilesFound(),
                    completedJob.getProfilesImported(),
                    completedJob.getDuplicatesIgnored(),
                    completedJob.getQueriesExecuted(),
                    completedJob.getPagesVisited(),
                    completedJob.getExecutionTimeMs());

            return discoveryJobDtoMapper.toResponse(completedJob);
        } catch (Exception exception) {
            long executionTimeMs = (System.nanoTime() - startedAt) / 1_000_000;
                String errorMessage = exception.getMessage();
                if (exception instanceof DiscoveryExecutionException discoveryExecutionException) {
                errorMessage = discoveryExecutionException.getFailureType() + ": " + discoveryExecutionException.getMessage();
                }

                job.fail(errorMessage, executionTimeMs);
            discoveryJobRepository.save(job);

            log.error("{} Job failed jobId={} executionTimeMs={} message={}",
                    ERROR,
                    job.getId(),
                    executionTimeMs,
                    errorMessage,
                    exception);
            throw exception;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
