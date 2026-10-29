package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.dto.RecruiterAnalyticsSummaryResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterCompanyAnalyticsResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.output.RecruiterCompanyStatistics;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecruiterAnalyticsServiceTest {

    private final RecruiterRepository recruiterRepository = mock(RecruiterRepository.class);
    private final RecruiterAnalyticsService service = new RecruiterAnalyticsService(recruiterRepository, new RecruiterDtoMapper());

    @Test
    void shouldReturnSummaryWhenThereAreNoRecruiters() {
        when(recruiterRepository.count()).thenReturn(0L);
        when(recruiterRepository.averageScore()).thenReturn(0.0);
        when(recruiterRepository.highestScore()).thenReturn(0);
        when(recruiterRepository.lowestScore()).thenReturn(0);
        when(recruiterRepository.countByStatus(RecruiterStatus.PENDING_REVIEW)).thenReturn(0L);
        when(recruiterRepository.countByStatus(RecruiterStatus.APPROVED)).thenReturn(0L);
        when(recruiterRepository.countByStatus(RecruiterStatus.INVITE_SENT)).thenReturn(0L);
        when(recruiterRepository.countByStatus(RecruiterStatus.CONNECTED)).thenReturn(0L);
        when(recruiterRepository.countByStatus(RecruiterStatus.DISMISSED)).thenReturn(0L);

        RecruiterAnalyticsSummaryResponse response = service.summary();

        assertThat(response.totalRecruiters()).isZero();
        assertThat(response.averageScore()).isZero();
        assertThat(response.highestScore()).isZero();
        assertThat(response.lowestScore()).isZero();
        assertThat(response.pendingReview()).isZero();
    }

    @Test
    void shouldReturnSummaryWhenThereAreRecruiters() {
        when(recruiterRepository.count()).thenReturn(5L);
        when(recruiterRepository.averageScore()).thenReturn(12.345);
        when(recruiterRepository.highestScore()).thenReturn(20);
        when(recruiterRepository.lowestScore()).thenReturn(5);
        when(recruiterRepository.countByStatus(RecruiterStatus.PENDING_REVIEW)).thenReturn(1L);
        when(recruiterRepository.countByStatus(RecruiterStatus.APPROVED)).thenReturn(1L);
        when(recruiterRepository.countByStatus(RecruiterStatus.INVITE_SENT)).thenReturn(1L);
        when(recruiterRepository.countByStatus(RecruiterStatus.CONNECTED)).thenReturn(1L);
        when(recruiterRepository.countByStatus(RecruiterStatus.DISMISSED)).thenReturn(1L);

        RecruiterAnalyticsSummaryResponse response = service.summary();

        assertThat(response.totalRecruiters()).isEqualTo(5);
        assertThat(response.averageScore()).isEqualTo(12.35);
        assertThat(response.highestScore()).isEqualTo(20);
        assertThat(response.lowestScore()).isEqualTo(5);
        assertThat(response.connected()).isEqualTo(1);
    }

    @Test
    void shouldReturnTopRecruiters() {
        Recruiter first = Recruiter.create("Ana", "Tech Recruiter", "Acme", "Sao Paulo", "https://linkedin.com/in/ana", "Hiring", null, 20);
        Recruiter second = Recruiter.create("Bruno", "Tech Recruiter", "Acme", "Sao Paulo", "https://linkedin.com/in/bruno", "Hiring", null, 15);
        when(recruiterRepository.findTopByScore(2)).thenReturn(List.of(first, second));

        List<RecruiterResponse> top = service.topRecruiters(2);

        assertThat(top).hasSize(2);
        assertThat(top.get(0).score()).isEqualTo(20);
        assertThat(top.get(1).score()).isEqualTo(15);
    }

    @Test
    void shouldReturnCompanyAnalyticsOrderedByCountDesc() {
        when(recruiterRepository.companyStatistics()).thenReturn(List.of(
                new RecruiterCompanyStatistics("Acme", 5, 12.0),
                new RecruiterCompanyStatistics("Globex", 2, 18.5)
        ));

        List<RecruiterCompanyAnalyticsResponse> companies = service.companyAnalytics();

        assertThat(companies).hasSize(2);
        assertThat(companies.get(0).company()).isEqualTo("Acme");
        assertThat(companies.get(0).recruiterCount()).isEqualTo(5);
        assertThat(companies.get(1).averageScore()).isEqualTo(18.5);
    }
}
