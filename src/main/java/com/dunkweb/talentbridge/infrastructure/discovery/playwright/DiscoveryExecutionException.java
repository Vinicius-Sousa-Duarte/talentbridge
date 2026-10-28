package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

public class DiscoveryExecutionException extends RuntimeException {

    private final DiscoveryFailureType failureType;

    public DiscoveryExecutionException(DiscoveryFailureType failureType, String message, Throwable cause) {
        super(message, cause);
        this.failureType = failureType;
    }

    public DiscoveryFailureType getFailureType() {
        return failureType;
    }
}
