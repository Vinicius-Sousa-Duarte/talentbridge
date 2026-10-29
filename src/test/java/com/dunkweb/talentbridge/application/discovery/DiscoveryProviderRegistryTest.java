package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderCapability;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscoveryProviderRegistryTest {

    @Test
    void shouldListNoProvidersWhenRegistryIsEmpty() {
        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of());

        assertThat(registry.listProviders()).isEmpty();
        assertThat(registry.findBySource(DiscoverySource.CSV)).isEmpty();
    }

    @Test
    void shouldRegisterAndFindProviderBySource() {
        DiscoveryProvider provider = new StubProvider(DiscoverySource.CSV);
        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(provider));

        assertThat(registry.listProviders()).hasSize(1);
        assertThat(registry.findBySource(DiscoverySource.CSV)).contains(provider);
    }

    @Test
    void shouldRejectDuplicateProviderSourceRegistration() {
        DiscoveryProviderRegistry registry = new DiscoveryProviderRegistry(List.of(new StubProvider(DiscoverySource.CSV)));

        assertThatThrownBy(() -> registry.register(new StubProvider(DiscoverySource.CSV)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Provider already registered");
    }

    private static class StubProvider implements DiscoveryProvider {

        private final DiscoverySource source;

        private StubProvider(DiscoverySource source) {
            this.source = source;
        }

        @Override
        public DiscoverySource source() {
            return source;
        }

        @Override
        public boolean supports(DiscoveryContext context) {
            return true;
        }

        @Override
        public ProviderCandidateBatch discover(DiscoveryContext context) {
            return ProviderCandidateBatch.empty();
        }

        @Override
        public boolean health() {
            return true;
        }

        @Override
        public List<ProviderCapability> capabilities() {
            return List.of();
        }
    }
}
