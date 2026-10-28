package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DiscoveryFailureClassifier {

    public DiscoveryFailureType classify(Throwable throwable) {
        String message = buildMessageChain(throwable);

        if (message.contains("captcha") || message.contains("checkpoint/challenge") || message.contains("security challenge") || message.contains("challenge")) {
            return DiscoveryFailureType.CAPTCHA;
        }
        if (message.contains("too many requests") || message.contains("rate limit") || message.contains("429")) {
            return DiscoveryFailureType.RATE_LIMIT;
        }
        if (message.contains("timeout") || message.contains("timed out")) {
            return DiscoveryFailureType.TIMEOUT;
        }
        if (message.contains("unauthorized") || message.contains("authentication") || message.contains("credencial") || message.contains("login") || message.contains("sign in") || message.contains("signin")) {
            return DiscoveryFailureType.AUTHENTICATION;
        }
        if (message.contains("selector") || message.contains("layout") || message.contains("element not found")) {
            return DiscoveryFailureType.LAYOUT_CHANGED;
        }
        if (message.contains("network") || message.contains("connection") || message.contains("socket") || message.contains("dns")) {
            return DiscoveryFailureType.NETWORK;
        }

        return DiscoveryFailureType.UNKNOWN;
    }

    private String buildMessageChain(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        int depth = 0;

        while (current != null && depth < 8) {
            if (current.getMessage() != null) {
                builder.append(current.getMessage().toLowerCase(Locale.ROOT)).append(' ');
            }
            current = current.getCause();
            depth++;
        }

        return builder.toString();
    }
}
