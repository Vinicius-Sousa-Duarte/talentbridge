package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.application.dto.ExecuteRecruiterDiscoveryRequest;
import com.dunkweb.talentbridge.application.mapper.DiscoveryJobDtoMapper;
import com.dunkweb.talentbridge.application.port.output.DiscoveryExtractionResult;
import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.application.port.output.RecruiterDiscoveryGateway;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.application.service.CooldownValidationService;
import com.dunkweb.talentbridge.application.service.DiscoveryOperationalLimitService;
import com.dunkweb.talentbridge.application.service.RecruiterDeduplicationService;
import com.dunkweb.talentbridge.application.service.SingleDiscoveryJobGuard;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecuteRecruiterDiscoveryUseCaseImplTest {

    @Mock
    private DiscoveryJobRepository discoveryJobRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RecruiterDiscoveryGateway recruiterDiscoveryGateway;

    @Mock
    private RecruiterDeduplicationService recruiterDeduplicationService;

    @Mock
    private RecruiterScoringEngine recruiterScoringEngine;

    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private SingleDiscoveryJobGuard singleDiscoveryJobGuard;

    @Mock
    private CooldownValidationService cooldownValidationService;

    @Mock
    private DiscoveryOperationalLimitService discoveryOperationalLimitService;

    private final DiscoveryJobDtoMapper discoveryJobDtoMapper = new DiscoveryJobDtoMapper();

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 500})
    void shouldImportRespectingRequestedProfilesLimit(int requestedProfiles) {
        UserProfile profile = sampleProfile();
        List<RecruiterProfile> extractedProfiles = generateProfiles(requestedProfiles + 20);

        when(discoveryJobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(singleDiscoveryJobGuard.runWithLock(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(discoveryOperationalLimitService.resolveDesiredProfiles(eq(requestedProfiles), anyString())).thenReturn(requestedProfiles);
        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(profile));
        when(recruiterDiscoveryGateway.discoverRecruiters(eq(profile), eq(requestedProfiles), any(UUID.class)))
            .thenReturn(new DiscoveryExtractionResult(extractedProfiles, 5, 12));
        when(recruiterDeduplicationService.isDuplicate(any(), anySet(), anySet())).thenReturn(false);
        when(recruiterScoringEngine.calculate(eq(profile), any(), any(), any())).thenReturn(10);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecuteRecruiterDiscoveryUseCaseImpl useCase = new ExecuteRecruiterDiscoveryUseCaseImpl(
                discoveryJobRepository,
                userProfileRepository,
                recruiterDiscoveryGateway,
                recruiterDeduplicationService,
                recruiterScoringEngine,
                recruiterRepository,
                discoveryJobDtoMapper,
                singleDiscoveryJobGuard,
                cooldownValidationService,
                discoveryOperationalLimitService
        );

        DiscoveryJobResponse response = useCase.execute(new ExecuteRecruiterDiscoveryRequest(requestedProfiles));

        assertThat(response.requestedProfiles()).isEqualTo(requestedProfiles);
        assertThat(response.profilesFound()).isEqualTo(requestedProfiles + 20);
        assertThat(response.profilesImported()).isEqualTo(requestedProfiles);
        assertThat(response.queriesExecuted()).isEqualTo(5);
        assertThat(response.pagesVisited()).isEqualTo(12);
        assertThat(response.status()).isEqualTo(DiscoveryJobStatus.COMPLETED);
        verify(recruiterRepository, times(requestedProfiles)).save(any(Recruiter.class));
    }

    @Test
    void shouldCountDuplicatesIgnored() {
        UserProfile profile = sampleProfile();
        List<RecruiterProfile> extractedProfiles = List.of(
                new RecruiterProfile("Ana", "Tech Recruiter", "Acme", "Sao Paulo", "https://linkedin.com/in/ana", "Hiring Java", null),
                new RecruiterProfile("Bruno", "Tech Recruiter", "CloudBridge", "Lisbon", "https://linkedin.com/in/bruno", "Hiring AWS", null),
                new RecruiterProfile("Carol", "Tech Recruiter", "Sky", "Porto", "https://linkedin.com/in/carol", "Hiring Spring", null)
        );

        when(discoveryJobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(singleDiscoveryJobGuard.runWithLock(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(discoveryOperationalLimitService.resolveDesiredProfiles(eq(3), anyString())).thenReturn(3);
        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(profile));
        when(recruiterDiscoveryGateway.discoverRecruiters(eq(profile), eq(3), any(UUID.class)))
            .thenReturn(new DiscoveryExtractionResult(extractedProfiles, 2, 3));
        when(recruiterDeduplicationService.isDuplicate(any(), anySet(), anySet())).thenReturn(true, false, true);
        when(recruiterScoringEngine.calculate(eq(profile), any(), any(), any())).thenReturn(8);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecuteRecruiterDiscoveryUseCaseImpl useCase = new ExecuteRecruiterDiscoveryUseCaseImpl(
                discoveryJobRepository,
                userProfileRepository,
                recruiterDiscoveryGateway,
                recruiterDeduplicationService,
                recruiterScoringEngine,
                recruiterRepository,
                discoveryJobDtoMapper,
                singleDiscoveryJobGuard,
                cooldownValidationService,
                discoveryOperationalLimitService
        );

        DiscoveryJobResponse response = useCase.execute(new ExecuteRecruiterDiscoveryRequest(3));

        assertThat(response.profilesFound()).isEqualTo(3);
        assertThat(response.profilesImported()).isEqualTo(1);
        assertThat(response.duplicatesIgnored()).isEqualTo(2);
        assertThat(response.queriesExecuted()).isEqualTo(2);
        assertThat(response.pagesVisited()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(DiscoveryJobStatus.COMPLETED);
        verify(recruiterRepository, times(1)).save(any(Recruiter.class));
    }

    @Test
    void shouldImportProfileWithoutHeadlineOrCompany() {
        UserProfile profile = sampleProfile();
        List<RecruiterProfile> extractedProfiles = List.of(
                new RecruiterProfile("Ana", null, null, "Sao Paulo", "https://linkedin.com/in/ana", null, null)
        );

        when(discoveryJobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(singleDiscoveryJobGuard.runWithLock(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(discoveryOperationalLimitService.resolveDesiredProfiles(eq(1), anyString())).thenReturn(1);
        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(profile));
        when(recruiterDiscoveryGateway.discoverRecruiters(eq(profile), eq(1), any(UUID.class)))
            .thenReturn(new DiscoveryExtractionResult(extractedProfiles, 1, 1));
        when(recruiterDeduplicationService.isDuplicate(any(), anySet(), anySet())).thenReturn(false);
        when(recruiterScoringEngine.calculate(eq(profile), any(), any(), any())).thenReturn(0);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecuteRecruiterDiscoveryUseCaseImpl useCase = new ExecuteRecruiterDiscoveryUseCaseImpl(
                discoveryJobRepository,
                userProfileRepository,
                recruiterDiscoveryGateway,
                recruiterDeduplicationService,
                recruiterScoringEngine,
                recruiterRepository,
                discoveryJobDtoMapper,
                singleDiscoveryJobGuard,
                cooldownValidationService,
                discoveryOperationalLimitService
        );

        DiscoveryJobResponse response = useCase.execute(new ExecuteRecruiterDiscoveryRequest(1));

        assertThat(response.profilesImported()).isEqualTo(1);
            assertThat(response.queriesExecuted()).isEqualTo(1);
            assertThat(response.pagesVisited()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(DiscoveryJobStatus.COMPLETED);
    }

    private UserProfile sampleProfile() {
        return UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );
    }

    private List<RecruiterProfile> generateProfiles(int size) {
        List<RecruiterProfile> profiles = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            profiles.add(new RecruiterProfile(
                    "Recruiter " + i,
                    "Tech Recruiter",
                    "Company " + i,
                    "Location " + i,
                    "https://linkedin.com/in/recruiter-" + i,
                    "Hiring Java",
                    null
            ));
        }
        return profiles;
    }
}
