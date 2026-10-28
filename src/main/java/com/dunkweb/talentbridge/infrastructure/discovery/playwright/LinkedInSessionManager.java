package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.domain.exception.BusinessException;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import com.dunkweb.talentbridge.shared.constants.LogConstants;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "talentbridge.discovery", name = "provider", havingValue = "playwright", matchIfMissing = true)
public class LinkedInSessionManager {

    private static final String LINKEDIN_LOGIN_URL = "https://www.linkedin.com/login";
    private static final String LINKEDIN_FEED_URL = "https://www.linkedin.com/feed/";
    private static final String USERNAME_SELECTOR = "input#username, input[name='session_key']";
    private static final String PASSWORD_SELECTOR = "input#password, input[name='session_password']";
    private static final String SUBMIT_SELECTOR = "button[type='submit'], button[data-litms-control-urn='login-submit']";
    private static final long MANUAL_CHALLENGE_POLL_INTERVAL_MS = 2_000L;
    private static final long MANUAL_CHALLENGE_MIN_TIMEOUT_MS = 120_000L;

    private final DiscoveryProperties discoveryProperties;
    private final LinkedInProperties linkedInProperties;
    private final HumanBehaviorSimulator humanBehaviorSimulator;

    public <T> T withAuthenticatedClient(UUID jobId, Function<LinkedInPageClient, T> operation) {
        try (Playwright playwright = Playwright.create()) {
            if (!hasStoredSession() && !hasConfiguredCredentials()) {
                runManualLoginBootstrap(playwright, jobId);
            }

            try {
                return runWithConfiguredClient(playwright, jobId, operation);
            } catch (BusinessException exception) {
                if (isCaptchaFailure(exception) && hasStoredSession()) {
                    log.warn("{} CAPTCHA detected while using persisted session. Invalidating storage state and retrying interactive login jobId={}",
                            LogConstants.LOGIN,
                            jobId);
                    invalidateStoredSession();
                    runManualLoginBootstrap(playwright, jobId);
                    return runWithConfiguredClient(playwright, jobId, operation);
                }
                throw exception;
            }
        }
    }

    private <T> T runWithConfiguredClient(
            Playwright playwright,
            UUID jobId,
            Function<LinkedInPageClient, T> operation
    ) {
        Browser browser = launchBrowser(playwright, discoveryProperties.headless());
        try {
            BrowserContext context = createContext(browser);
            try {
                Page page = context.newPage();
                ensureAuthenticated(page, context, jobId);
                LinkedInPageClient client = new PlaywrightLinkedInPageClient(
                        page,
                        discoveryProperties.pageTimeout(),
                        humanBehaviorSimulator
                );
                return operation.apply(client);
            } finally {
                context.close();
            }
        } finally {
            browser.close();
        }
    }

    private Browser launchBrowser(Playwright playwright, boolean headless) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    private boolean hasConfiguredCredentials() {
        return linkedInProperties.email() != null && !linkedInProperties.email().isBlank()
                && linkedInProperties.password() != null && !linkedInProperties.password().isBlank();
    }

    private boolean hasStoredSession() {
        return Files.exists(storageStatePath());
    }

    private void invalidateStoredSession() {
        try {
            Files.deleteIfExists(storageStatePath());
        } catch (Exception exception) {
            log.warn("{} Failed to invalidate LinkedIn storage state message={}",
                    LogConstants.ERROR,
                    exception.getMessage());
        }
    }

    private boolean isCaptchaFailure(Throwable throwable) {
        Throwable current = throwable;
        int depth = 0;

        while (current != null && depth < 8) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                if (normalized.contains("captcha") || normalized.contains("challenge")) {
                    return true;
                }
            }
            current = current.getCause();
            depth++;
        }

        return false;
    }

    private void runManualLoginBootstrap(Playwright playwright, UUID jobId) {
        log.info("{} Starting interactive login bootstrap jobId={} mode=headed-only-login", LogConstants.LOGIN, jobId);

        Browser headedBrowser = launchBrowser(playwright, false);
        try {
            BrowserContext headedContext = createContext(headedBrowser);
            try {
                Page loginPage = headedContext.newPage();
                ensureAuthenticated(loginPage, headedContext, jobId);
            } finally {
                headedContext.close();
            }
        } finally {
            headedBrowser.close();
        }

        if (!hasStoredSession()) {
            throw new ValidationException("Login manual nao foi concluido. Nenhuma sessao autenticada foi persistida.");
        }

        log.info("{} Interactive login bootstrap completed jobId={}", LogConstants.LOGIN, jobId);
    }

    private BrowserContext createContext(Browser browser) {
        Path storageStatePath = storageStatePath();

        if (Files.exists(storageStatePath)) {
            return browser.newContext(new Browser.NewContextOptions().setStorageStatePath(storageStatePath));
        }
        return browser.newContext();
    }

    private void ensureAuthenticated(Page page, BrowserContext context, UUID jobId) {
        page.navigate(LINKEDIN_FEED_URL, new Page.NavigateOptions().setTimeout((double) discoveryProperties.pageTimeout()));
        humanBehaviorSimulator.sleepRandom();

        if (isAuthenticated(page)) {
            log.info("{} Session restored jobId={}", LogConstants.LOGIN, jobId);
            return;
        }

        doLogin(page, context, jobId);
    }

    private void doLogin(Page page, BrowserContext context, UUID jobId) {
        page.navigate(LINKEDIN_LOGIN_URL, new Page.NavigateOptions().setTimeout((double) discoveryProperties.pageTimeout()));
        humanBehaviorSimulator.sleepRandom();

        if (isAuthenticated(page)) {
            log.info("{} Login skipped because session is already authenticated jobId={}", LogConstants.LOGIN, jobId);
            return;
        }

        if (!hasConfiguredCredentials()) {
            if (discoveryProperties.headless()) {
                throw new ValidationException("Credenciais ausentes e login manual indisponivel em headless=true.");
            }

            log.warn("{} LinkedIn credentials not configured. Waiting for manual login in browser window jobId={}",
                    LogConstants.LOGIN,
                    jobId);

            if (!waitForManualAuthentication(page, jobId, "credentials-missing")) {
                throw new ValidationException("Login manual nao concluido dentro da janela de tempo.");
            }

            persistStorageState(context);
            log.info("{} Login completed jobId={} viaManualAuthentication=true", LogConstants.LOGIN, jobId);
            return;
        }

        if (isBlockingChallengePage(page)) {
            boolean resolved = attemptManualChallengeResolution(page, jobId, "before-login-form");
            if (resolved && isAuthenticated(page)) {
                persistStorageState(context);
                log.info("{} Login completed jobId={} viaManualChallengeResolution=true", LogConstants.LOGIN, jobId);
                return;
            }
            if (isBlockingChallengePage(page)) {
                if (discoveryProperties.headless()) {
                    throw new BusinessException("CAPTCHA detected before login form rendering. Manual resolution requires headless=false.");
                }
                throw new BusinessException("CAPTCHA detected before login form rendering.");
            }
        }

        Locator usernameField = page.locator(USERNAME_SELECTOR).first();
        usernameField.waitFor(new Locator.WaitForOptions().setTimeout((double) discoveryProperties.pageTimeout()));
        usernameField.fill(linkedInProperties.email());
        humanBehaviorSimulator.sleepRandom();

        Locator passwordField = page.locator(PASSWORD_SELECTOR).first();
        passwordField.waitFor(new Locator.WaitForOptions().setTimeout((double) discoveryProperties.pageTimeout()));
        passwordField.fill(linkedInProperties.password());
        humanBehaviorSimulator.sleepRandom();

        Locator submitButton = page.locator(SUBMIT_SELECTOR).first();
        submitButton.waitFor(new Locator.WaitForOptions().setTimeout((double) discoveryProperties.pageTimeout()));
        submitButton.click();
        humanBehaviorSimulator.sleepRandom();
        page.waitForLoadState(LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout((double) discoveryProperties.pageTimeout()));

        if (isBlockingChallengePage(page)) {
            boolean resolved = attemptManualChallengeResolution(page, jobId, "after-login-submit");
            if (resolved && isAuthenticated(page)) {
                persistStorageState(context);
                log.info("{} Login completed jobId={} viaManualChallengeResolution=true", LogConstants.LOGIN, jobId);
                return;
            }
            if (discoveryProperties.headless()) {
                throw new BusinessException("CAPTCHA detected during login. Manual resolution requires headless=false.");
            }
            throw new BusinessException("CAPTCHA detected during login.");
        }

        if (!isAuthenticated(page)) {
            throw new BusinessException("Falha ao autenticar no LinkedIn. Verifique credenciais e desafio de login.");
        }

        persistStorageState(context);

        log.info("{} Login completed jobId={}", LogConstants.LOGIN, jobId);
    }

    private boolean waitForManualAuthentication(Page page, UUID jobId, String phase) {
        long configuredWindow = (long) discoveryProperties.pageTimeout() * 8;
        long windowMs = Math.max(MANUAL_CHALLENGE_MIN_TIMEOUT_MS, configuredWindow);
        long deadline = System.currentTimeMillis() + windowMs;

        log.warn("{} Waiting for manual LinkedIn authentication jobId={} phase={} windowMs={} action=Complete login in opened browser",
                LogConstants.LOGIN,
                jobId,
                phase,
                windowMs);

        while (System.currentTimeMillis() < deadline) {
            if (isAuthenticated(page)) {
                return true;
            }

            try {
                page.waitForTimeout((double) MANUAL_CHALLENGE_POLL_INTERVAL_MS);
            } catch (Exception exception) {
                log.warn("{} Failed while waiting manual authentication jobId={} phase={} message={}",
                        LogConstants.LOGIN,
                        jobId,
                        phase,
                        exception.getMessage());
                return false;
            }
        }

        return isAuthenticated(page);
    }

    private boolean attemptManualChallengeResolution(Page page, UUID jobId, String phase) {
        if (discoveryProperties.headless()) {
            log.warn("{} CAPTCHA challenge detected but manual resolution is unavailable in headless mode jobId={} phase={}",
                    LogConstants.LOGIN,
                    jobId,
                    phase);
            return false;
        }

        long configuredWindow = (long) discoveryProperties.pageTimeout() * 6;
        long windowMs = Math.max(MANUAL_CHALLENGE_MIN_TIMEOUT_MS, configuredWindow);
        long deadline = System.currentTimeMillis() + windowMs;

        log.warn("{} CAPTCHA challenge detected jobId={} phase={} manualResolutionWindowMs={} action=Resolve challenge in opened browser",
                LogConstants.LOGIN,
                jobId,
                phase,
                windowMs);

        while (System.currentTimeMillis() < deadline) {
            if (isAuthenticated(page)) {
                log.info("{} CAPTCHA resolved and session authenticated jobId={} phase={}",
                        LogConstants.LOGIN,
                        jobId,
                        phase);
                return true;
            }

            if (!isBlockingChallengePage(page) && isLoginFormVisible(page)) {
                log.info("{} CAPTCHA challenge cleared and login form is visible again jobId={} phase={}",
                        LogConstants.LOGIN,
                        jobId,
                        phase);
                return false;
            }

            try {
                page.waitForTimeout((double) MANUAL_CHALLENGE_POLL_INTERVAL_MS);
            } catch (Exception exception) {
                log.warn("{} Failed while waiting manual challenge resolution jobId={} phase={} message={}",
                        LogConstants.LOGIN,
                        jobId,
                        phase,
                        exception.getMessage());
                return false;
            }
        }

        return isAuthenticated(page);
    }

    private boolean isLoginFormVisible(Page page) {
        try {
            return page.locator(USERNAME_SELECTOR).count() > 0 && page.locator(PASSWORD_SELECTOR).count() > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void persistStorageState(BrowserContext context) {
        try {
            Path storagePath = storageStatePath();
            Files.createDirectories(storagePath.getParent());
            context.storageState(new BrowserContext.StorageStateOptions().setPath(storagePath));
        } catch (Exception exception) {
            log.warn("{} Falha ao salvar storage state do LinkedIn message={}",
                    LogConstants.ERROR,
                    exception.getMessage());
        }
    }

    private boolean isAuthenticated(Page page) {
        String url = page.url() == null ? "" : page.url();
        if (url.contains("/feed") && !url.contains("/login")) {
            return true;
        }

        try {
            return page.locator("div.global-nav").count() > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Path storageStatePath() {
        return Path.of("target", "playwright", "linkedin-storage-state.json");
    }

    private boolean isBlockingChallengePage(Page page) {
        String normalizedUrl = page.url() == null ? "" : page.url().toLowerCase(Locale.ROOT);
        if (normalizedUrl.contains("captcha") || normalizedUrl.contains("checkpoint/challenge")) {
            return true;
        }

        try {
            String content = page.content();
            if (content == null) {
                return false;
            }

            String normalizedContent = content.toLowerCase(Locale.ROOT);
            return normalizedContent.contains("captcha") || normalizedContent.contains("security challenge");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static class PlaywrightLinkedInPageClient implements LinkedInPageClient {

        private static final List<String> CARD_SELECTORS = List.of(
                "li.reusable-search__result-container",
                "li.search-result",
                "div.search-results-container li"
        );

        private static final List<String> NAME_SELECTORS = List.of(
                "span.entity-result__title-text a span[aria-hidden='true']",
                "span[dir='ltr'] span[aria-hidden='true']",
                "a.app-aware-link span[aria-hidden='true']"
        );

        private static final List<String> HEADLINE_SELECTORS = List.of(
                "div.entity-result__primary-subtitle",
                "div.t-14.t-black.t-normal",
                "p.subline-level-1"
        );

        private static final List<String> LOCATION_SELECTORS = List.of(
                "div.entity-result__secondary-subtitle",
                "div.t-14.t-normal span[aria-hidden='true']",
                "p.subline-level-2"
        );

        private static final List<String> LINK_SELECTORS = List.of(
                "a.app-aware-link[href*='/in/']",
                "a[href*='/in/']"
        );

        private static final List<String> EMPTY_STATE_SELECTORS = List.of(
            "div.search-no-results",
            "p.t-14.t-black--light",
            "div[data-view-name='search-results-container']"
        );

        private final Page page;
        private final int timeout;
        private final HumanBehaviorSimulator humanBehaviorSimulator;

        private PlaywrightLinkedInPageClient(Page page, int timeout, HumanBehaviorSimulator humanBehaviorSimulator) {
            this.page = page;
            this.timeout = timeout;
            this.humanBehaviorSimulator = humanBehaviorSimulator;
        }

        @Override
        public void openSearch(String query) {
            humanBehaviorSimulator.sleepRandom();
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUrl = "https://www.linkedin.com/search/results/people/?keywords=" + encodedQuery;
            page.navigate(searchUrl, new Page.NavigateOptions().setTimeout((double) timeout));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout((double) timeout));
            waitForSearchReady();
            validateBlockedSignals();
        }

        @Override
        public List<RecruiterProfile> extractRecruitersFromCurrentPage() {
            Locator cards = firstLocator(CARD_SELECTORS);
            int cardsCount = cards.count();
            List<RecruiterProfile> extracted = new ArrayList<>();

            for (int index = 0; index < cardsCount; index++) {
                Locator card = cards.nth(index);
                String name = normalizeText(firstText(card, NAME_SELECTORS));
                String headline = normalizeText(firstText(card, HEADLINE_SELECTORS));
                String location = normalizeText(firstText(card, LOCATION_SELECTORS));
                String linkedinUrl = normalizeLinkedinUrl(firstAttribute(card, LINK_SELECTORS, "href"));

                String position = extractPosition(headline);
                String company = extractCompany(headline);

                extracted.add(new RecruiterProfile(
                        name,
                        position,
                        company,
                        location,
                        linkedinUrl,
                        headline,
                        null
                ));
            }

            return extracted;
        }

        @Override
        public boolean goToNextPage() {
            Locator nextButton = firstLocator(List.of(
                    "button[aria-label='Next']",
                    "button.artdeco-pagination__button--next"
            ));

            if (nextButton.count() == 0 || nextButton.first().isDisabled()) {
                return false;
            }

            humanBehaviorSimulator.sleepRandom();
            nextButton.first().click(new Locator.ClickOptions().setTimeout((double) timeout));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout((double) timeout));
            waitForSearchReady();
            validateBlockedSignals();
            return true;
        }

        private void waitForSearchReady() {
            long waitBudgetMs = Math.max(5_000L, Math.round(timeout * 0.6d));
            long deadline = System.currentTimeMillis() + waitBudgetMs;

            while (System.currentTimeMillis() < deadline) {
                validateBlockedSignals();

                if (firstLocator(CARD_SELECTORS).count() > 0) {
                    return;
                }

                if (firstLocator(EMPTY_STATE_SELECTORS).count() > 0) {
                    return;
                }

                page.waitForTimeout(300d);
            }
        }

        private void validateBlockedSignals() {
            String normalizedUrl = page.url() == null ? "" : page.url().toLowerCase(Locale.ROOT);
            if (normalizedUrl.contains("captcha") || normalizedUrl.contains("checkpoint/challenge")) {
                throw new RuntimeException("CAPTCHA detected while navigating LinkedIn pages.");
            }

            try {
                String content = page.content();
                if (content == null) {
                    return;
                }

                String normalizedContent = content.toLowerCase(Locale.ROOT);
                if (normalizedContent.contains("too many requests") || normalizedContent.contains("rate limit")) {
                    throw new RuntimeException("RATE_LIMIT detected while navigating LinkedIn pages.");
                }
                if (normalizedContent.contains("captcha") || normalizedContent.contains("security challenge")) {
                    throw new RuntimeException("CAPTCHA detected while navigating LinkedIn pages.");
                }
            } catch (RuntimeException runtimeException) {
                throw runtimeException;
            } catch (Exception ignored) {
                // Ignore content read failures and continue flow.
            }
        }

        private Locator firstLocator(List<String> selectors) {
            for (String selector : selectors) {
                Locator locator = page.locator(selector);
                if (locator.count() > 0) {
                    return locator;
                }
            }
            return page.locator("__invalid_selector__");
        }

        private String firstText(Locator root, List<String> selectors) {
            for (String selector : selectors) {
                Locator candidate = root.locator(selector);
                if (candidate.count() > 0) {
                    try {
                        String text = candidate.first().innerText();
                        if (text != null && !text.isBlank()) {
                            return text;
                        }
                    } catch (Exception ignored) {
                        // Ignore transient selector/read issues and continue trying fallback selectors.
                    }
                }
            }
            return null;
        }

        private String firstAttribute(Locator root, List<String> selectors, String attributeName) {
            for (String selector : selectors) {
                Locator candidate = root.locator(selector);
                if (candidate.count() > 0) {
                    try {
                        String attribute = candidate.first().getAttribute(attributeName);
                        if (attribute != null && !attribute.isBlank()) {
                            return attribute;
                        }
                    } catch (Exception ignored) {
                        // Ignore transient selector/read issues and continue trying fallback selectors.
                    }
                }
            }
            return null;
        }

        private String normalizeLinkedinUrl(String rawUrl) {
            if (rawUrl == null || rawUrl.isBlank()) {
                return null;
            }

            String normalized = rawUrl.trim();
            int queryIndex = normalized.indexOf('?');
            if (queryIndex >= 0) {
                normalized = normalized.substring(0, queryIndex);
            }
            return normalized;
        }

        private String normalizeText(String text) {
            if (text == null || text.isBlank()) {
                return null;
            }
            return text.trim().replaceAll("\\s+", " ");
        }

        private String extractPosition(String headline) {
            if (headline == null || headline.isBlank()) {
                return null;
            }
            String lower = headline.toLowerCase();
            int marker = lower.indexOf(" at ");
            if (marker > 0) {
                return headline.substring(0, marker).trim();
            }
            return headline;
        }

        private String extractCompany(String headline) {
            if (headline == null || headline.isBlank()) {
                return null;
            }
            String lower = headline.toLowerCase();
            int marker = lower.lastIndexOf(" at ");
            if (marker < 0 || marker + 4 >= headline.length()) {
                return null;
            }
            return headline.substring(marker + 4).trim();
        }
    }
}
