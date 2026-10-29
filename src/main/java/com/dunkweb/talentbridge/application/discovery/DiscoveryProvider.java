package com.dunkweb.talentbridge.application.discovery;

import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryContext;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderCapability;

import java.util.List;

public interface DiscoveryProvider {

    DiscoverySource source();

    boolean supports(DiscoveryContext context);

    ProviderCandidateBatch discover(DiscoveryContext context);

    boolean health();

    List<ProviderCapability> capabilities();
}
