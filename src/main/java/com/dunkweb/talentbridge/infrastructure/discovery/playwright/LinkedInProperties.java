package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "linkedin")
public record LinkedInProperties(
        String email,
        String password
) {
}
