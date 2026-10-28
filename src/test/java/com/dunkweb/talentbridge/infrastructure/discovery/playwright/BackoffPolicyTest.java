package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackoffPolicyTest {

    @Test
    void shouldGenerateExponentialDelays() {
        BackoffPolicy policy = new BackoffPolicy();

        assertThat(policy.delaySecondsForAttempt(1)).isEqualTo(2);
        assertThat(policy.delaySecondsForAttempt(2)).isEqualTo(4);
        assertThat(policy.delaySecondsForAttempt(3)).isEqualTo(8);
    }

    @Test
    void shouldSleepWithExpectedDelayMillis() {
        List<Long> delays = new ArrayList<>();
        BackoffPolicy policy = new BackoffPolicy(delays::add);

        policy.sleepBeforeRetry(1);
        policy.sleepBeforeRetry(2);
        policy.sleepBeforeRetry(3);

        assertThat(delays).containsExactly(2_000L, 4_000L, 8_000L);
    }
}
