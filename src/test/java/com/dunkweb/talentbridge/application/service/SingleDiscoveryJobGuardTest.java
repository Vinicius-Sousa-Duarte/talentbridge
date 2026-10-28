package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleDiscoveryJobGuardTest {

    @Test
    void shouldBlockConcurrentDiscoveryJob() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        when(repository.existsByStatus(DiscoveryJobStatus.RUNNING)).thenReturn(true);

        SingleDiscoveryJobGuard guard = new SingleDiscoveryJobGuard(repository);

        assertThatThrownBy(guard::ensureNoRunningJob)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DiscoveryJob em execução");
    }

    @Test
    void shouldRunOperationInsideLock() {
        DiscoveryJobRepository repository = mock(DiscoveryJobRepository.class);
        SingleDiscoveryJobGuard guard = new SingleDiscoveryJobGuard(repository);

        String value = guard.runWithLock(() -> "ok");

        assertThat(value).isEqualTo("ok");
    }
}
