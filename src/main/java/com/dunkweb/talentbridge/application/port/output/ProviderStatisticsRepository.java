package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.ProviderStatistics;

import java.util.List;
import java.util.Optional;

public interface ProviderStatisticsRepository {

    ProviderStatistics save(ProviderStatistics statistics);

    Optional<ProviderStatistics> findByProviderName(String providerName);

    List<ProviderStatistics> findAll();
}
