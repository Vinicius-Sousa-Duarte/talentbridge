package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "talentbridge.discovery")
public record DiscoveryProperties(
        boolean enabled,
        String provider,
        boolean headless,
        int pageTimeout,
        int maxPagesPerSearch,
        int cooldownMinutes,
        int minDelaySeconds,
        int maxDelaySeconds,
        int maxProfilesPerExecution,
        int maxProfilesPerDay,
        int maxQueriesPerExecution
) {
}
