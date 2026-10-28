package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import com.dunkweb.talentbridge.application.port.output.DiscoveryExtractionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaywrightRecruiterDiscoveryGatewayTest {

    @Mock
    private RecruiterSearchQueryBuilder queryBuilder;

    @Mock
    private LinkedInSessionManager linkedInSessionManager;

    @Mock
    private LinkedInPageClient linkedInPageClient;

    @Mock
    private DiscoveryFailureClassifier discoveryFailureClassifier;

    @Mock
    private BackoffPolicy backoffPolicy;

    @Test
    void shouldReturnEmptyWhenDiscoveryIsDisabled() {
        DiscoveryProperties properties = new DiscoveryProperties(false, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);
        PlaywrightRecruiterDiscoveryGateway gateway = new PlaywrightRecruiterDiscoveryGateway(
                properties,
                queryBuilder,
                linkedInSessionManager,
                discoveryFailureClassifier,
                backoffPolicy
        );

        DiscoveryExtractionResult result = gateway.discoverRecruiters(null, 100, UUID.randomUUID());

        assertThat(result.profiles()).isEmpty();
        assertThat(result.queriesExecuted()).isZero();
        assertThat(result.pagesVisited()).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDiscoverProfilesUsingLinkedInSessionManager() {
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 2, 30, 2, 7, 100, 150, 20);
        PlaywrightRecruiterDiscoveryGateway gateway = new PlaywrightRecruiterDiscoveryGateway(
            properties,
            queryBuilder,
            linkedInSessionManager,
            discoveryFailureClassifier,
            backoffPolicy
        );

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        UUID jobId = UUID.randomUUID();

        when(queryBuilder.buildQueries(eq(profile), anyInt())).thenReturn(List.of("Tech Recruiter Java"));
        when(backoffPolicy.maxAttempts()).thenReturn(3);
        when(linkedInPageClient.extractRecruitersFromCurrentPage()).thenReturn(List.of(
                new RecruiterProfile("Ana", "Tech Recruiter", "Acme", "Sao Paulo", "https://linkedin.com/in/ana", "Hiring Java", null),
                new RecruiterProfile("Bruno", "Talent Acquisition", "CloudBridge", "Lisbon", "https://linkedin.com/in/bruno", "Hiring AWS", null)
        ));

        when(linkedInSessionManager.withAuthenticatedClient(eq(jobId), any()))
                .thenAnswer(invocation -> {
                                        Function<LinkedInPageClient, ?> callback = invocation.getArgument(1);
                    return callback.apply(linkedInPageClient);
                });

        DiscoveryExtractionResult result = gateway.discoverRecruiters(profile, 1, jobId);

        assertThat(result.profiles()).hasSize(2);
        assertThat(result.queriesExecuted()).isEqualTo(1);
        assertThat(result.pagesVisited()).isEqualTo(1);
        verify(linkedInPageClient).openSearch("Tech Recruiter Java");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldStopImmediatelyWhenCaptchaIsDetected() {
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 2, 30, 2, 7, 100, 150, 20);
        PlaywrightRecruiterDiscoveryGateway gateway = new PlaywrightRecruiterDiscoveryGateway(
                properties,
                queryBuilder,
                linkedInSessionManager,
                discoveryFailureClassifier,
                backoffPolicy
        );

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        UUID jobId = UUID.randomUUID();

        when(queryBuilder.buildQueries(eq(profile), anyInt())).thenReturn(List.of("Tech Recruiter Java"));
        when(backoffPolicy.maxAttempts()).thenReturn(3);
        when(discoveryFailureClassifier.classify(any())).thenReturn(DiscoveryFailureType.CAPTCHA);
        doThrow(new RuntimeException("captcha detected")).when(linkedInPageClient).openSearch("Tech Recruiter Java");
        when(linkedInSessionManager.withAuthenticatedClient(eq(jobId), any()))
                .thenAnswer(invocation -> {
                    Function<LinkedInPageClient, ?> callback = invocation.getArgument(1);
                    return callback.apply(linkedInPageClient);
                });

        assertThatThrownBy(() -> gateway.discoverRecruiters(profile, 10, jobId))
                .isInstanceOf(DiscoveryExecutionException.class);

        verify(backoffPolicy, never()).sleepBeforeRetry(anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldWrapSessionBootstrapFailureAsDiscoveryExecutionException() {
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 2, 30, 2, 7, 100, 150, 20);
        PlaywrightRecruiterDiscoveryGateway gateway = new PlaywrightRecruiterDiscoveryGateway(
                properties,
                queryBuilder,
                linkedInSessionManager,
                discoveryFailureClassifier,
                backoffPolicy
        );

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        UUID jobId = UUID.randomUUID();

        when(queryBuilder.buildQueries(eq(profile), anyInt())).thenReturn(List.of("Tech Recruiter Java"));
        when(discoveryFailureClassifier.classify(any())).thenReturn(DiscoveryFailureType.TIMEOUT);
        when(linkedInSessionManager.withAuthenticatedClient(eq(jobId), any()))
                .thenThrow(new RuntimeException("Timeout 30000ms exceeded while waiting for login field"));

        assertThatThrownBy(() -> gateway.discoverRecruiters(profile, 10, jobId))
                .isInstanceOf(DiscoveryExecutionException.class)
                .satisfies(throwable -> assertThat(((DiscoveryExecutionException) throwable).getFailureType())
                        .isEqualTo(DiscoveryFailureType.TIMEOUT));
    }
}
