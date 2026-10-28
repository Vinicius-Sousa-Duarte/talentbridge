package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

public enum DiscoveryFailureType {
    TIMEOUT,
    AUTHENTICATION,
    CAPTCHA,
    RATE_LIMIT,
    LAYOUT_CHANGED,
    NETWORK,
    UNKNOWN
}
