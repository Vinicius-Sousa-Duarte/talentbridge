package com.dunkweb.talentbridge.application.dto;

import java.util.List;

public record DiscoveryProviderHealthResponse(
        List<DiscoveryProviderHealthItemResponse> providers
) {
}
