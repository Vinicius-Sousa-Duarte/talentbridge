package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.service.ProviderStatisticsService;
import com.dunkweb.talentbridge.application.service.RecruiterAnalyticsService;
import com.dunkweb.talentbridge.domain.entity.ProviderStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderStatisticsService providerStatisticsService;

        @MockBean
        private RecruiterAnalyticsService recruiterAnalyticsService;

    @Test
    void shouldReturnProvidersAnalyticsList() throws Exception {
        when(providerStatisticsService.listAll()).thenReturn(List.of(
                stats("CSV", 10, 10, 0, 50, 42, 8, 95.0, 14.5),
                stats("MANUAL", 6, 5, 1, 12, 10, 2, 70.0, 18.5)
        ));

        mockMvc.perform(get("/api/analytics/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("CSV"))
                .andExpect(jsonPath("$[0].executions").value(10))
                .andExpect(jsonPath("$[0].averageExecutionTimeMs").value(95))
                .andExpect(jsonPath("$[0].averageScore").value(14.5));
    }

    @Test
    void shouldReturnProviderDetails() throws Exception {
        when(providerStatisticsService.getByProvider("CSV")).thenReturn(
                stats("CSV", 2, 2, 0, 4, 3, 1, 90.0, 16.25)
        );

        mockMvc.perform(get("/api/analytics/providers/CSV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("CSV"))
                .andExpect(jsonPath("$.averageScore").value(16.25))
                .andExpect(jsonPath("$.candidatesImported").value(3));
    }

    @Test
    void shouldReturnProvidersRanking() throws Exception {
        when(providerStatisticsService.ranking()).thenReturn(List.of(
                stats("MANUAL", 5, 5, 0, 15, 11, 4, 60.0, 18.5),
                stats("CSV", 10, 10, 0, 50, 42, 8, 95.0, 12.0)
        ));

        mockMvc.perform(get("/api/analytics/providers/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("MANUAL"))
                .andExpect(jsonPath("$[0].averageScore").value(18.5))
                .andExpect(jsonPath("$[1].provider").value("CSV"));
    }

    @Test
    void shouldReturnRecruitersAnalyticsSummary() throws Exception {
        when(recruiterAnalyticsService.summary()).thenReturn(new com.dunkweb.talentbridge.application.dto.RecruiterAnalyticsSummaryResponse(
                10,
                14.5,
                20,
                5,
                2,
                3,
                1,
                3,
                1
        ));

        mockMvc.perform(get("/api/analytics/recruiters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecruiters").value(10))
                .andExpect(jsonPath("$.averageScore").value(14.5))
                .andExpect(jsonPath("$.connected").value(3));
    }

    @Test
    void shouldReturnTopRecruiters() throws Exception {
        when(recruiterAnalyticsService.topRecruiters(2)).thenReturn(List.of(
                new com.dunkweb.talentbridge.application.dto.RecruiterResponse(
                        UUID.randomUUID(),
                        "Ana",
                        "Tech Recruiter",
                        "Acme",
                        "Sao Paulo",
                        "https://linkedin.com/in/ana",
                        "Hiring",
                        null,
                        20,
                        com.dunkweb.talentbridge.domain.enums.RecruiterStatus.PENDING_REVIEW,
                        LocalDateTime.now(),
                        null,
                        null
                ),
                new com.dunkweb.talentbridge.application.dto.RecruiterResponse(
                        UUID.randomUUID(),
                        "Bruno",
                        "Tech Recruiter",
                        "Globex",
                        "Sao Paulo",
                        "https://linkedin.com/in/bruno",
                        "Hiring",
                        null,
                        15,
                        com.dunkweb.talentbridge.domain.enums.RecruiterStatus.APPROVED,
                        LocalDateTime.now(),
                        null,
                        null
                )
        ));

        mockMvc.perform(get("/api/analytics/recruiters/top").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].score").value(20))
                .andExpect(jsonPath("$[1].score").value(15));
    }

    @Test
    void shouldReturnRecruitersCompanyAnalytics() throws Exception {
        when(recruiterAnalyticsService.companyAnalytics()).thenReturn(List.of(
                new com.dunkweb.talentbridge.application.dto.RecruiterCompanyAnalyticsResponse("Acme", 7, 13.2),
                new com.dunkweb.talentbridge.application.dto.RecruiterCompanyAnalyticsResponse("Globex", 3, 11.0)
        ));

        mockMvc.perform(get("/api/analytics/recruiters/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].company").value("Acme"))
                .andExpect(jsonPath("$[0].recruiterCount").value(7))
                .andExpect(jsonPath("$[0].averageScore").value(13.2));
    }

    private ProviderStatistics stats(
            String provider,
            long executions,
            long successful,
            long failed,
            long found,
            long imported,
            long duplicates,
            double avgTime,
            double avgScore
    ) {
        return new ProviderStatistics(
                java.util.UUID.randomUUID(),
                provider,
                executions,
                successful,
                failed,
                found,
                imported,
                duplicates,
                Math.round(avgTime * executions),
                avgScore * Math.max(1, found),
                Math.max(1, found),
                avgTime,
                avgScore,
                LocalDateTime.now()
        );
    }
}
