package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.shared.constants.LogConstants;
import com.dunkweb.talentbridge.shared.util.SensitiveDataMasker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${talentbridge.discovery.enabled:true}' == 'true' and '${talentbridge.discovery.provider:playwright}' == 'playwright'")
public class LinkedInCredentialsStartupValidator {

    private final LinkedInProperties linkedInProperties;

    @PostConstruct
    void validate() {
        String email = linkedInProperties.email();
        String password = linkedInProperties.password();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            if (Files.exists(storageStatePath())) {
                log.info("{} LinkedIn credentials not configured, but persisted authenticated session was found.",
                        LogConstants.LOGIN);
                return;
            }

            log.warn("{} LinkedIn credentials not configured. Discovery will require interactive login to establish a session.",
                    LogConstants.LOGIN);
            return;
        }

        log.info("{} LinkedIn credentials validated account={}",
                LogConstants.LOGIN,
                SensitiveDataMasker.maskEmail(email));
    }

    private Path storageStatePath() {
        return Path.of("target", "playwright", "linkedin-storage-state.json");
    }
}
