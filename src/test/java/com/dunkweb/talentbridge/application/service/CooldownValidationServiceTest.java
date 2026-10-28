package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.entity.DiscoveryJob;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import com.dunkweb.talentbridge.infrastructure.discovery.playwright.DiscoveryProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CooldownValidationServiceTest {

    @Test
    void shouldBlockExecutionWhenCooldownIsActive() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);

        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:15:00Z"), ZoneOffset.UTC);
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 27, 14, 0, 0);

        when(repository.findLatestCompleted()).thenReturn(Optional.of(sampleCompletedJob(createdAt)));

        CooldownValidationService service = new CooldownValidationService(repository, properties, clock);

        assertThatThrownBy(service::validate)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cooldown");
    }

    @Test
    void shouldAllowExecutionWhenCooldownExpired() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);

        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:45:00Z"), ZoneOffset.UTC);
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 27, 14, 0, 0);

        when(repository.findLatestCompleted()).thenReturn(Optional.of(sampleCompletedJob(createdAt)));

        CooldownValidationService service = new CooldownValidationService(repository, properties, clock);

        assertThatCode(service::validate).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowExecutionWhenLatestJobFailed() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);

        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:10:00Z"), ZoneOffset.UTC);
        when(repository.findLatestCompleted()).thenReturn(Optional.empty());

        CooldownValidationService service = new CooldownValidationService(repository, properties, clock);

        assertThatCode(service::validate).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowExecutionWhenLatestJobFailedByCaptcha() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);

        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:10:00Z"), ZoneOffset.UTC);
        when(repository.findLatestCompleted()).thenReturn(Optional.empty());

        CooldownValidationService service = new CooldownValidationService(repository, properties, clock);

        assertThatCode(service::validate).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowExecutionWhenLatestJobFailedByTimeout() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);

        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:10:00Z"), ZoneOffset.UTC);
        when(repository.findLatestCompleted()).thenReturn(Optional.empty());

        CooldownValidationService service = new CooldownValidationService(repository, properties, clock);

        assertThatCode(service::validate).doesNotThrowAnyException();
    }

    private DiscoveryJob sampleCompletedJob(LocalDateTime createdAt) {
        return DiscoveryJob.restore(
                UUID.randomUUID(),
                100,
                80,
                60,
                20,
                10,
                4,
                30_000L,
                DiscoveryJobStatus.COMPLETED,
                null,
                createdAt,
                createdAt.plusMinutes(2)
        );
    }
}
