package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import com.dunkweb.talentbridge.infrastructure.discovery.playwright.DiscoveryProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscoveryOperationalLimitServiceTest {

    @Test
    void shouldApplyPerExecutionLimitWhenRequestedIsHigher() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        when(repository.sumImportedProfilesSince(org.mockito.ArgumentMatchers.any())).thenReturn(10);

        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);
        DiscoveryOperationalLimitService service = new DiscoveryOperationalLimitService(repository, properties);

        int result = service.resolveDesiredProfiles(300, "job-1");

        assertThat(result).isEqualTo(100);
    }

    @Test
    void shouldBlockWhenDailyLimitIsReached() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        when(repository.sumImportedProfilesSince(org.mockito.ArgumentMatchers.any())).thenReturn(150);

        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);
        DiscoveryOperationalLimitService service = new DiscoveryOperationalLimitService(repository, properties);

        assertThatThrownBy(() -> service.resolveDesiredProfiles(50, "job-2"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Limite diario");
    }
}
