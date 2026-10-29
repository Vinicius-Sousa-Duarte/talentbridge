package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.application.service.ProviderStatisticsService;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderCapability;
import com.dunkweb.talentbridge.domain.valueobject.discovery.RecruiterCandidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryOrchestratorTest {

    @Mock
    private RecruiterScoringEngine recruiterScoringEngine;

    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ProviderStatisticsService providerStatisticsService;

    @Test
    void shouldReturnEmptyResultWhenNoProvidersAreRegistered() {
        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of());
        DiscoveryOrchestrator orchestrator = orchestrator(registry);

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.totalCandidatesFound()).isZero();
        assertThat(result.uniqueCandidatesFound()).isZero();
        assertThat(result.importedCandidates()).isZero();
        verify(recruiterRepository, times(0)).save(any(Recruiter.class));
    }

    @Test
    void shouldExecuteMultipleProvidersAndPersistUniqueCandidates() {
        DiscoveryProvider csv = new TestProvider(
                DiscoverySource.CSV,
                true,
                false,
                List.of(
                        candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana"),
                        candidate("CSV", "2", "Bruno", "Globex", "https://linkedin.com/in/bruno")
                )
        );
        DiscoveryProvider manual = new TestProvider(
                DiscoverySource.MANUAL,
                true,
                false,
                List.of(candidate("MANUAL", "3", "Carla", "Initech", "https://linkedin.com/in/carla"))
        );

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(csv, manual));
        DiscoveryOrchestrator orchestrator = orchestrator(registry);
        when(recruiterScoringEngine.calculate(any(), any(), any(), any())).thenReturn(10);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(sampleProfile()));

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.totalCandidatesFound()).isEqualTo(3);
        assertThat(result.uniqueCandidatesFound()).isEqualTo(3);
        assertThat(result.importedCandidates()).isEqualTo(3);
        assertThat(result.providerResults()).hasSize(2);
        verify(recruiterRepository, times(3)).save(any(Recruiter.class));
    }

    @Test
    void shouldIgnoreDuplicateCandidatesAcrossProviders() {
        DiscoveryProvider csv = new TestProvider(
                DiscoverySource.CSV,
                true,
                false,
                List.of(candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana"))
        );
        DiscoveryProvider manual = new TestProvider(
                DiscoverySource.MANUAL,
                true,
                false,
                List.of(candidate("MANUAL", "2", "Ana Oliveira", "Acme", "https://linkedin.com/in/ana"))
        );

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(csv, manual));
        DiscoveryOrchestrator orchestrator = orchestrator(registry);
        when(recruiterScoringEngine.calculate(any(), any(), any(), any())).thenReturn(8);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.totalCandidatesFound()).isEqualTo(2);
        assertThat(result.uniqueCandidatesFound()).isEqualTo(1);
        assertThat(result.duplicatesIgnored()).isEqualTo(1);
        assertThat(result.importedCandidates()).isEqualTo(1);
        verify(recruiterRepository, times(1)).save(any(Recruiter.class));
    }

    @Test
    void shouldContinueWhenProviderFails() {
        DiscoveryProvider failing = new TestProvider(
                DiscoverySource.CSV,
                true,
                true,
                List.of()
        );
        DiscoveryProvider healthy = new TestProvider(
                DiscoverySource.MANUAL,
                true,
                false,
                List.of(candidate("MANUAL", "3", "Carlos", "Sky", "https://linkedin.com/in/carlos"))
        );

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(failing, healthy));
        DiscoveryOrchestrator orchestrator = orchestrator(registry);
        when(recruiterScoringEngine.calculate(any(), any(), any(), any())).thenReturn(7);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.providerResults()).hasSize(2);
        assertThat(result.providerResults().stream().anyMatch(provider -> !provider.successful())).isTrue();
        assertThat(result.importedCandidates()).isEqualTo(1);
    }

    @Test
    void shouldSkipUnavailableProvider() {
        DiscoveryProvider unavailable = new TestProvider(
                DiscoverySource.CSV,
                false,
                false,
                List.of(candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana"))
        );

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(unavailable));
        DiscoveryOrchestrator orchestrator = orchestrator(registry);

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.totalCandidatesFound()).isZero();
        assertThat(result.providerResults()).hasSize(1);
        assertThat(result.providerResults().get(0).successful()).isFalse();
        assertThat(result.providerResults().get(0).warnings()).contains("Provider unavailable");
    }

    @Test
    void shouldIgnoreCandidatesThatAlreadyExistInRepository() {
        DiscoveryProvider csv = new TestProvider(
                DiscoverySource.CSV,
                true,
                false,
                List.of(candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana"))
        );

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(csv));
        DiscoveryOrchestrator orchestrator = orchestrator(registry);

        when(recruiterScoringEngine.calculate(any(), any(), any(), any())).thenReturn(10);
        when(recruiterRepository.existsByLinkedinUrl(anyString())).thenReturn(true);

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.totalCandidatesFound()).isEqualTo(1);
        assertThat(result.uniqueCandidatesFound()).isEqualTo(1);
        assertThat(result.importedCandidates()).isZero();
        assertThat(result.duplicatesIgnored()).isEqualTo(1);
        verify(recruiterRepository, times(0)).save(any(Recruiter.class));
    }

    @Test
    void shouldIgnoreCandidatesThatAlreadyExistByNameAndCompany() {
        DiscoveryProvider csv = new TestProvider(
                DiscoverySource.CSV,
                true,
                false,
                List.of(candidate("CSV", "1", "Ana", "Acme", "https://linkedin.com/in/ana"))
        );

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(csv));
        DiscoveryOrchestrator orchestrator = orchestrator(registry);

        when(recruiterScoringEngine.calculate(any(), any(), any(), any())).thenReturn(10);
        when(recruiterRepository.existsByLinkedinUrl(anyString())).thenReturn(false);
        when(recruiterRepository.existsByNameAndCompany("Ana", "Acme")).thenReturn(true);

        DiscoveryResult result = orchestrator.execute(sampleContext());

        assertThat(result.totalCandidatesFound()).isEqualTo(1);
        assertThat(result.uniqueCandidatesFound()).isEqualTo(1);
        assertThat(result.importedCandidates()).isZero();
        assertThat(result.duplicatesIgnored()).isEqualTo(1);
        verify(recruiterRepository, times(0)).save(any(Recruiter.class));
    }

    private DiscoveryOrchestrator orchestrator(DiscoveryProviderRegistry registry) {
        return new DiscoveryOrchestrator(
                registry,
                new DefaultCandidateNormalizer(),
                new DefaultCandidateDeduplicationService(),
                recruiterScoringEngine,
                recruiterRepository,
                userProfileRepository,
                providerStatisticsService
        );
    }

    private DiscoveryContext sampleContext() {
        return new DiscoveryContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of("Java", "Spring Boot"),
                List.of("Tech Recruiter"),
                List.of("SOLUTIONS_ARCHITECT"),
                50,
                100,
                20,
                Duration.ofSeconds(30),
                "corr-123",
                Map.of()
        );
    }

    private UserProfile sampleProfile() {
        return UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );
    }

    private RecruiterCandidate candidate(
            String provider,
            String sourceRef,
            String name,
            String company,
            String linkedinUrl
    ) {
        return new RecruiterCandidate(
                UUID.randomUUID(),
                provider,
                DiscoverySource.UNKNOWN,
                sourceRef,
                name,
                "Tech Recruiter",
                company,
                "Sao Paulo",
                "Hiring Java",
                linkedinUrl,
                0.8,
                Instant.now(),
                Map.of(),
                Map.of()
        );
    }

    private static class TestProvider implements DiscoveryProvider {

        private final DiscoverySource source;
        private final boolean healthy;
        private final boolean failOnDiscover;
        private final List<RecruiterCandidate> candidates;

        private TestProvider(
                DiscoverySource source,
                boolean healthy,
                boolean failOnDiscover,
                List<RecruiterCandidate> candidates
        ) {
            this.source = source;
            this.healthy = healthy;
            this.failOnDiscover = failOnDiscover;
            this.candidates = candidates;
        }

        @Override
        public DiscoverySource source() {
            return source;
        }

        @Override
        public boolean supports(DiscoveryContext context) {
            return true;
        }

        @Override
        public ProviderCandidateBatch discover(DiscoveryContext context) {
            if (failOnDiscover) {
                throw new RuntimeException("Provider failure");
            }
            return new ProviderCandidateBatch(candidates, candidates.size(), candidates.size(), 0, List.of(), List.of());
        }

        @Override
        public boolean health() {
            return healthy;
        }

        @Override
        public List<ProviderCapability> capabilities() {
            return List.of();
        }
    }
}
