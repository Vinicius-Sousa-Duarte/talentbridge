package com.dunkweb.talentbridge.infrastructure.configuration;

import com.dunkweb.talentbridge.shared.constants.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupLogger {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        String activeProfiles = environment.getActiveProfiles().length == 0
                ? "default"
                : String.join(",", environment.getActiveProfiles());

        log.info("{} Application started name={} environment={} javaVersion={} activeProfiles={}",
                LogConstants.APPLICATION,
                environment.getProperty("spring.application.name", "talent-bridge"),
                environment.getProperty("spring.profiles.active", activeProfiles),
                System.getProperty("java.version"),
                activeProfiles);
    }
}
