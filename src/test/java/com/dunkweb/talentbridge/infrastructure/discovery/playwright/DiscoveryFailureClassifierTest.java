package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryFailureClassifierTest {

    private final DiscoveryFailureClassifier classifier = new DiscoveryFailureClassifier();

    @Test
    void shouldClassifyCaptchaFailure() {
        DiscoveryFailureType type = classifier.classify(new RuntimeException("CAPTCHA detected while navigating LinkedIn pages."));

        assertThat(type).isEqualTo(DiscoveryFailureType.CAPTCHA);
    }

    @Test
    void shouldClassifyRateLimitFailure() {
        DiscoveryFailureType type = classifier.classify(new RuntimeException("429 too many requests from gateway"));

        assertThat(type).isEqualTo(DiscoveryFailureType.RATE_LIMIT);
    }

    @Test
    void shouldClassifyTimeoutFailure() {
        DiscoveryFailureType type = classifier.classify(new RuntimeException("Operation timed out while loading page"));

        assertThat(type).isEqualTo(DiscoveryFailureType.TIMEOUT);
    }

    @Test
    void shouldPrioritizeCaptchaWhenMessageAlsoContainsLogin() {
        DiscoveryFailureType type = classifier.classify(
                new RuntimeException("CAPTCHA detected before login form rendering.")
        );

        assertThat(type).isEqualTo(DiscoveryFailureType.CAPTCHA);
    }

    @Test
    void shouldClassifyCaptchaFromCauseChain() {
        DiscoveryFailureType type = classifier.classify(
                new RuntimeException(
                        "LinkedIn session bootstrap failed before query execution.",
                        new RuntimeException("security challenge captcha")
                )
        );

        assertThat(type).isEqualTo(DiscoveryFailureType.CAPTCHA);
    }
}
