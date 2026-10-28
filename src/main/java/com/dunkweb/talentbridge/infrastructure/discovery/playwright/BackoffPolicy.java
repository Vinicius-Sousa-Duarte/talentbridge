package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.domain.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class BackoffPolicy {

    public static final int MAX_ATTEMPTS = 3;

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final Sleeper sleeper;

    public BackoffPolicy() {
        this(Thread::sleep);
    }

    BackoffPolicy(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    public int maxAttempts() {
        return MAX_ATTEMPTS;
    }

    public long delaySecondsForAttempt(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("Attempt must be >= 1");
        }
        return 1L << attempt;
    }

    public void sleepBeforeRetry(int attempt) {
        long delayMillis = delaySecondsForAttempt(attempt) * 1_000;
        try {
            sleeper.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Retry interrupted while waiting for backoff.");
        }
    }
}
