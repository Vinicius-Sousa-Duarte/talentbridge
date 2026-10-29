package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.dto.RecruiterAnalyticsSummaryResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterCompanyAnalyticsResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.ANALYTICS;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruiterAnalyticsService {

    private final RecruiterRepository recruiterRepository;
    private final RecruiterDtoMapper recruiterDtoMapper;

    public RecruiterAnalyticsSummaryResponse summary() {
        long total = recruiterRepository.count();

        RecruiterAnalyticsSummaryResponse response = new RecruiterAnalyticsSummaryResponse(
                total,
                roundDouble(recruiterRepository.averageScore()),
                recruiterRepository.highestScore(),
                recruiterRepository.lowestScore(),
                recruiterRepository.countByStatus(RecruiterStatus.PENDING_REVIEW),
                recruiterRepository.countByStatus(RecruiterStatus.APPROVED),
                recruiterRepository.countByStatus(RecruiterStatus.INVITE_SENT),
                recruiterRepository.countByStatus(RecruiterStatus.CONNECTED),
                recruiterRepository.countByStatus(RecruiterStatus.DISMISSED)
        );

        log.info("{} Recruiter analytics summary generated totalRecruiters={} averageScore={}",
                ANALYTICS,
                response.totalRecruiters(),
                response.averageScore());

        return response;
    }

    public List<RecruiterResponse> topRecruiters(int limit) {
        int safeLimit = Math.max(1, limit);
        List<RecruiterResponse> response = recruiterRepository.findTopByScore(safeLimit).stream()
                .map(recruiterDtoMapper::toResponse)
                .toList();

        log.info("{} Top recruiters generated limit={} returned={}", ANALYTICS, safeLimit, response.size());

        return response;
    }

    public List<RecruiterCompanyAnalyticsResponse> companyAnalytics() {
        List<RecruiterCompanyAnalyticsResponse> response = recruiterRepository.companyStatistics().stream()
                .map(item -> new RecruiterCompanyAnalyticsResponse(
                        item.company(),
                        item.recruiterCount(),
                        roundDouble(item.averageScore())
                ))
                .toList();

        log.info("{} Recruiter company analytics generated companies={}", ANALYTICS, response.size());
        return response;
    }

    private double roundDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
