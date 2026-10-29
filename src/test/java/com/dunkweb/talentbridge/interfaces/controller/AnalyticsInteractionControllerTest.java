package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.service.ProviderStatisticsService;
import com.dunkweb.talentbridge.application.service.RecruiterAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AnalyticsController.class, InteractionController.class})
class AnalyticsInteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderStatisticsService providerStatisticsService;

    @MockBean
    private RecruiterAnalyticsService recruiterAnalyticsService;

    @Test
    void shouldReturnNotImplementedForAnalyticsEndpoints() throws Exception {
        mockMvc.perform(get("/api/analytics/discovery")).andExpect(status().isNotImplemented());
    }

    @Test
    void shouldReturnNotImplementedForInteractionEndpoints() throws Exception {
        mockMvc.perform(post("/api/interactions")).andExpect(status().isNotImplemented());
        mockMvc.perform(get("/api/interactions/{id}", UUID.randomUUID())).andExpect(status().isNotImplemented());
    }
}
