package com.dunkweb.talentbridge.domain.valueobject.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;

import java.util.Map;

public record ProviderCapability(
        DiscoverySource source,
        boolean interactiveAuthentication,
        boolean realtime,
        Map<String, Object> attributes
) {
    public ProviderCapability {
        source = source == null ? DiscoverySource.UNKNOWN : source;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
