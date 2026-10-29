package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.discovery.DefaultCandidateDeduplicationService;
import com.dunkweb.talentbridge.application.discovery.DefaultCandidateNormalizer;
import com.dunkweb.talentbridge.application.discovery.DiscoveryOrchestrator;
import com.dunkweb.talentbridge.application.discovery.DiscoveryProviderRegistry;
import com.dunkweb.talentbridge.application.discovery.ManualDiscoveryProvider;
import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.application.service.ProviderStatisticsService;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportManualDiscoveryUseCaseTest {

    @Test
    void shouldRunFullPipelineWithManualProviderAndPersistCandidate() {
        RecruiterScoringEngine scoringEngine = mock(RecruiterScoringEngine.class);
        RecruiterRepository recruiterRepository = mock(RecruiterRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        KeywordRepository keywordRepository = mock(KeywordRepository.class);
        ProviderStatisticsService providerStatisticsService = mock(ProviderStatisticsService.class);

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(new ManualDiscoveryProvider()));
        DiscoveryOrchestrator orchestrator = new DiscoveryOrchestrator(
                registry,
                new DefaultCandidateNormalizer(),
                new DefaultCandidateDeduplicationService(),
                scoringEngine,
                recruiterRepository,
                userProfileRepository,
                providerStatisticsService
        );

        ImportManualDiscoveryUseCaseImpl useCase = new ImportManualDiscoveryUseCaseImpl(
                orchestrator,
                userProfileRepository,
                keywordRepository
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.empty());
        when(keywordRepository.findAll()).thenReturn(List.of(Keyword.create("Java")));
        when(scoringEngine.calculate(any(), anyString(), anyString(), any())).thenReturn(77);
        when(recruiterRepository.existsByLinkedinUrl(any())).thenReturn(false);
        when(recruiterRepository.existsByNameAndCompany(any(), any())).thenReturn(false);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscoveryResult result = useCase.execute(new CreateManualCandidateRequest(
                "Lucas Ferreira",
                "Tech Recruiter",
                "Mercado Livre",
                "Sao Paulo",
                "Hiring Java Engineers",
                "https://linkedin.com/in/lucas"
        ), "req-123");

        assertThat(result.totalCandidatesFound()).isEqualTo(1);
        assertThat(result.uniqueCandidatesFound()).isEqualTo(1);
        assertThat(result.importedCandidates()).isEqualTo(1);
        assertThat(result.candidates()).hasSize(1);
        assertThat(result.candidates().get(0).provider()).isEqualTo("MANUAL");
        assertThat(result.candidates().get(0).normalizedFields().get("score")).isEqualTo(77);

        verify(scoringEngine, times(1)).calculate(any(), anyString(), anyString(), any());
        verify(recruiterRepository, times(1)).save(any(Recruiter.class));
    }

    @Test
    void shouldIgnoreDuplicateCandidateInRepository() {
        RecruiterScoringEngine scoringEngine = mock(RecruiterScoringEngine.class);
        RecruiterRepository recruiterRepository = mock(RecruiterRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        KeywordRepository keywordRepository = mock(KeywordRepository.class);
        ProviderStatisticsService providerStatisticsService = mock(ProviderStatisticsService.class);

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(new ManualDiscoveryProvider()));
        DiscoveryOrchestrator orchestrator = new DiscoveryOrchestrator(
                registry,
                new DefaultCandidateNormalizer(),
                new DefaultCandidateDeduplicationService(),
                scoringEngine,
                recruiterRepository,
                userProfileRepository,
                providerStatisticsService
        );

        ImportManualDiscoveryUseCaseImpl useCase = new ImportManualDiscoveryUseCaseImpl(
                orchestrator,
                userProfileRepository,
                keywordRepository
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.empty());
        when(keywordRepository.findAll()).thenReturn(List.of());
        when(scoringEngine.calculate(any(), anyString(), anyString(), any())).thenReturn(65);
        when(recruiterRepository.existsByLinkedinUrl(any())).thenReturn(true);

        DiscoveryResult result = useCase.execute(new CreateManualCandidateRequest(
                "Lucas Ferreira",
                "Tech Recruiter",
                "Mercado Livre",
                "Sao Paulo",
                "Hiring Java Engineers",
                "https://linkedin.com/in/lucas"
        ), "req-dup");

        assertThat(result.importedCandidates()).isZero();
        assertThat(result.duplicatesIgnored()).isEqualTo(1);
        verify(recruiterRepository, never()).save(any(Recruiter.class));
    }

    @Test
    void shouldFailWhenNameIsMissing() {
        RecruiterScoringEngine scoringEngine = mock(RecruiterScoringEngine.class);
        RecruiterRepository recruiterRepository = mock(RecruiterRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        KeywordRepository keywordRepository = mock(KeywordRepository.class);
        ProviderStatisticsService providerStatisticsService = mock(ProviderStatisticsService.class);

        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(new ManualDiscoveryProvider()));
        DiscoveryOrchestrator orchestrator = new DiscoveryOrchestrator(
                registry,
                new DefaultCandidateNormalizer(),
                new DefaultCandidateDeduplicationService(),
                scoringEngine,
                recruiterRepository,
                userProfileRepository,
                providerStatisticsService
        );

        ImportManualDiscoveryUseCaseImpl useCase = new ImportManualDiscoveryUseCaseImpl(
                orchestrator,
                userProfileRepository,
                keywordRepository
        );

        assertThatThrownBy(() -> useCase.execute(new CreateManualCandidateRequest(
                " ",
                "Tech Recruiter",
                "Mercado Livre",
                "Sao Paulo",
                "Hiring Java Engineers",
                "https://linkedin.com/in/lucas"
        ), "req-invalid"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Nome");
    }
}
