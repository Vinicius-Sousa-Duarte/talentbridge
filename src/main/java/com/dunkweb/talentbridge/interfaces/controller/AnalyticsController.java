package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.dto.ProviderAnalyticsResponse;
import com.dunkweb.talentbridge.application.dto.ProviderRankingResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterAnalyticsSummaryResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterCompanyAnalyticsResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.service.ProviderStatisticsService;
import com.dunkweb.talentbridge.application.service.RecruiterAnalyticsService;
import com.dunkweb.talentbridge.domain.entity.ProviderStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.ANALYTICS;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER_ANALYTICS;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

@RestController
@RequestMapping("/api/analytics")
@Slf4j
@RequiredArgsConstructor
public class AnalyticsController {

    private final ProviderStatisticsService providerStatisticsService;
    private final RecruiterAnalyticsService recruiterAnalyticsService;

    @GetMapping("/discovery")
    public ResponseEntity<Void> discoveryAnalytics() {
        log.info("{} Endpoint called endpoint=/api/analytics/discovery requestId={}",
                ANALYTICS,
                MDC.get(REQUEST_ID));
        throw new ResponseStatusException(NOT_IMPLEMENTED, "Endpoint ainda nao implementado.");
    }

    @GetMapping("/providers")
        public ResponseEntity<List<ProviderAnalyticsResponse>> providersAnalytics() {
        log.info("{} Provider analytics list requested endpoint=/api/analytics/providers requestId={}",
                ANALYTICS,
                MDC.get(REQUEST_ID));

        List<ProviderAnalyticsResponse> response = providerStatisticsService.listAll().stream()
            .map(this::toAnalyticsResponse)
            .toList();

        log.info("{} Provider analytics list generated endpoint=/api/analytics/providers requestId={} providers={}",
            PROVIDER_ANALYTICS,
            MDC.get(REQUEST_ID),
            response.stream().map(ProviderAnalyticsResponse::provider).toList());

        return ResponseEntity.ok(response);
        }

        @GetMapping("/providers/{provider}")
        public ResponseEntity<ProviderAnalyticsResponse> providerDetails(@PathVariable String provider) {
        log.info("{} Provider analytics details requested endpoint=/api/analytics/providers/{{provider}} requestId={} provider={}",
            ANALYTICS,
            MDC.get(REQUEST_ID),
            provider);

        ProviderStatistics statistics = providerStatisticsService.getByProvider(provider);
        return ResponseEntity.ok(toAnalyticsResponse(statistics));
        }

        @GetMapping("/providers/ranking")
        public ResponseEntity<List<ProviderRankingResponse>> providerRanking() {
        log.info("{} Provider ranking requested endpoint=/api/analytics/providers/ranking requestId={}",
            ANALYTICS,
            MDC.get(REQUEST_ID));

        List<ProviderRankingResponse> ranking = providerStatisticsService.ranking().stream()
            .map(statistics -> new ProviderRankingResponse(
                statistics.providerName(),
                roundDouble(statistics.averageScore())
            ))
            .toList();

        log.info("{} Ranking generated endpoint=/api/analytics/providers/ranking requestId={} providers={}",
            PROVIDER_ANALYTICS,
            MDC.get(REQUEST_ID),
            ranking.stream().map(ProviderRankingResponse::provider).toList());

        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/recruiters")
    public ResponseEntity<RecruiterAnalyticsSummaryResponse> recruitersAnalytics() {
        log.info("{} Recruiter analytics summary requested endpoint=/api/analytics/recruiters requestId={}",
                ANALYTICS,
                MDC.get(REQUEST_ID));
        return ResponseEntity.ok(recruiterAnalyticsService.summary());
    }

    @GetMapping("/recruiters/top")
    public ResponseEntity<List<RecruiterResponse>> topRecruiters(
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        int safeLimit = limit == null ? 10 : Math.max(1, limit);
        log.info("{} Top recruiters analytics requested endpoint=/api/analytics/recruiters/top requestId={} limit={}",
                ANALYTICS,
                MDC.get(REQUEST_ID),
                safeLimit);
        return ResponseEntity.ok(recruiterAnalyticsService.topRecruiters(safeLimit));
    }

    @GetMapping("/recruiters/companies")
    public ResponseEntity<List<RecruiterCompanyAnalyticsResponse>> recruiterCompaniesAnalytics() {
        log.info("{} Recruiter companies analytics requested endpoint=/api/analytics/recruiters/companies requestId={}",
                ANALYTICS,
                MDC.get(REQUEST_ID));
        return ResponseEntity.ok(recruiterAnalyticsService.companyAnalytics());
    }

    private ProviderAnalyticsResponse toAnalyticsResponse(ProviderStatistics statistics) {
        return new ProviderAnalyticsResponse(
                statistics.providerName(),
                statistics.executions(),
                statistics.successfulExecutions(),
                statistics.failedExecutions(),
                statistics.candidatesFound(),
                statistics.candidatesImported(),
                statistics.duplicatesIgnored(),
                Math.round(statistics.averageExecutionTimeMs()),
                roundDouble(statistics.averageScore()),
                statistics.lastExecutionAt()
        );
    }

    private double roundDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
