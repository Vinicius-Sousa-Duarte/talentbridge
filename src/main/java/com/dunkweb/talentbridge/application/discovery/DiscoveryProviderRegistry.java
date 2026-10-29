package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DiscoveryProviderRegistry {

    private final Map<DiscoverySource, DiscoveryProvider> providers = new EnumMap<>(DiscoverySource.class);

    public DiscoveryProviderRegistry(List<DiscoveryProvider> providers) {
        if (providers == null) {
            return;
        }

        providers.forEach(this::register);
    }

    public synchronized void register(DiscoveryProvider provider) {
        if (provider == null) {
            return;
        }

        DiscoverySource source = provider.source();
        if (providers.containsKey(source)) {
            throw new IllegalStateException("Provider already registered for source " + source);
        }

        providers.put(source, provider);
    }

    public Optional<DiscoveryProvider> findBySource(DiscoverySource source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(source));
    }

    public List<DiscoveryProvider> listProviders() {
        return new ArrayList<>(providers.values());
    }
}
