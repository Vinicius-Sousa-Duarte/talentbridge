package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.domain.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class HumanBehaviorSimulator {

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final DiscoveryProperties discoveryProperties;
    private final Sleeper sleeper;

    @Autowired
    public HumanBehaviorSimulator(DiscoveryProperties discoveryProperties) {
        this(discoveryProperties, Thread::sleep);
    }

    HumanBehaviorSimulator(DiscoveryProperties discoveryProperties, Sleeper sleeper) {
        this.discoveryProperties = discoveryProperties;
        this.sleeper = sleeper;
    }

    public int randomDelaySeconds() {
        int min = Math.max(0, discoveryProperties.minDelaySeconds());
        int max = Math.max(min, discoveryProperties.maxDelaySeconds());
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public void sleepRandom() {
        int delaySeconds = randomDelaySeconds();
        try {
            sleeper.sleep(delaySeconds * 1_000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Humanized delay interrupted.");
        }
    }
}
