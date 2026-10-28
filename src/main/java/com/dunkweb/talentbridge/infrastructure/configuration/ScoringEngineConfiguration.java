package com.dunkweb.talentbridge.infrastructure.configuration;

import com.dunkweb.talentbridge.domain.service.DefaultRecruiterScoringEngine;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScoringEngineConfiguration {

    @Bean
    public RecruiterScoringEngine recruiterScoringEngine() {
        return new DefaultRecruiterScoringEngine();
    }
}
