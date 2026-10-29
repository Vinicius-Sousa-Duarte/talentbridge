package com.dunkweb.talentbridge.application.port.input;

import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;

public interface ImportCsvDiscoveryUseCase {

    DiscoveryResult execute(String csvContent, Integer desiredProfiles, String correlationId);
}
