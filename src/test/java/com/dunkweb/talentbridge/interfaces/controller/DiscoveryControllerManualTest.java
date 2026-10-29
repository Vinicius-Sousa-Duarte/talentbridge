package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.discovery.DiscoveryProviderRegistry;
import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.application.port.input.ImportCsvDiscoveryUseCase;
import com.dunkweb.talentbridge.application.port.input.ImportManualDiscoveryUseCase;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import com.dunkweb.talentbridge.domain.valueobject.discovery.ProviderExecutionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscoveryController.class)
class DiscoveryControllerManualTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImportCsvDiscoveryUseCase importCsvDiscoveryUseCase;

    @MockBean
    private ImportManualDiscoveryUseCase importManualDiscoveryUseCase;

    @MockBean
    private DiscoveryProviderRegistry discoveryProviderRegistry;

    @Test
    void shouldImportManualCandidateWithValidPayload() throws Exception {
        DiscoveryResult result = new DiscoveryResult(
                UUID.randomUUID(),
                1,
                1,
                1,
                0,
                10L,
                List.of(new ProviderExecutionResult("MANUAL", 1, 1, 0, 1, 1, 0, 10L, true, List.of(), List.of())),
                List.of()
        );
        when(importManualDiscoveryUseCase.execute(any(CreateManualCandidateRequest.class), anyString())).thenReturn(result);

        mockMvc.perform(post("/api/discovery/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManualCandidateRequest(
                                "Lucas Ferreira",
                                "Tech Recruiter",
                                "Mercado Livre",
                                "Sao Paulo",
                                "Hiring Java Engineers",
                                "https://linkedin.com/in/lucas"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCandidates").value(1))
                .andExpect(jsonPath("$.providerResults[0].providerName").value("MANUAL"));
    }

    @Test
    void shouldImportManualCandidateWithMinimumPayload() throws Exception {
        DiscoveryResult result = new DiscoveryResult(
                UUID.randomUUID(),
                1,
                1,
                0,
                1,
                8L,
                List.of(new ProviderExecutionResult("MANUAL", 1, 1, 0, 1, 0, 1, 8L, true, List.of(), List.of())),
                List.of()
        );
        when(importManualDiscoveryUseCase.execute(any(CreateManualCandidateRequest.class), anyString())).thenReturn(result);

        mockMvc.perform(post("/api/discovery/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManualCandidateRequest(
                                "Lucas Ferreira",
                                null,
                                null,
                                null,
                                null,
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidatesFound").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
        mockMvc.perform(post("/api/discovery/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManualCandidateRequest(
                                " ",
                                "Tech Recruiter",
                                "Mercado Livre",
                                "Sao Paulo",
                                "Hiring Java Engineers",
                                "https://linkedin.com/in/lucas"
                        ))))
                .andExpect(status().isBadRequest());
    }
}
