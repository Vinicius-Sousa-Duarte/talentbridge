package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class HumanBehaviorSimulatorTest {

    @Test
    void shouldGenerateRandomDelaysWithinConfiguredRange() {
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 2, 7, 100, 150, 20);
        HumanBehaviorSimulator simulator = new HumanBehaviorSimulator(properties, millis -> {
        });

        Set<Integer> generated = new HashSet<>();
        for (int index = 0; index < 80; index++) {
            int delay = simulator.randomDelaySeconds();
            assertThat(delay).isBetween(2, 7);
            generated.add(delay);
        }

        assertThat(generated.size()).isGreaterThan(1);
    }

    @Test
    void shouldSleepUsingGeneratedRandomDelay() {
        DiscoveryProperties properties = new DiscoveryProperties(true, "playwright", true, 30_000, 10, 30, 3, 3, 100, 150, 20);
        AtomicLong sleptMillis = new AtomicLong(0);
        HumanBehaviorSimulator simulator = new HumanBehaviorSimulator(properties, sleptMillis::set);

        simulator.sleepRandom();

        assertThat(sleptMillis.get()).isEqualTo(3_000L);
    }
}
