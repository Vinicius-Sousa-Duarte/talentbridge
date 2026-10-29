package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.discovery.DiscoveryProvider;
import com.dunkweb.talentbridge.application.discovery.DiscoveryProviderRegistry;
import com.dunkweb.talentbridge.application.port.input.ImportCsvDiscoveryUseCase;
import com.dunkweb.talentbridge.application.port.input.ImportManualDiscoveryUseCase;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscoveryController.class)
class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImportCsvDiscoveryUseCase importCsvDiscoveryUseCase;

    @MockBean
    private ImportManualDiscoveryUseCase importManualDiscoveryUseCase;

    @MockBean
    private DiscoveryProviderRegistry discoveryProviderRegistry;

    @Test
    void shouldListProvidersWithEnabledFlag() throws Exception {
        DiscoveryProvider csvProvider = org.mockito.Mockito.mock(DiscoveryProvider.class);
        when(csvProvider.source()).thenReturn(DiscoverySource.CSV);
        when(discoveryProviderRegistry.listProviders()).thenReturn(List.of(csvProvider));

        mockMvc.perform(get("/api/discovery/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("CSV"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].name").value("MANUAL"))
                .andExpect(jsonPath("$[1].enabled").value(false))
                .andExpect(jsonPath("$[2].name").value("GOOGLE"))
                .andExpect(jsonPath("$[2].enabled").value(false))
                .andExpect(jsonPath("$[3].name").value("BING"))
                .andExpect(jsonPath("$[3].enabled").value(false))
                .andExpect(jsonPath("$[4].name").value("LINKEDIN"))
                .andExpect(jsonPath("$[4].enabled").value(false))
                .andExpect(jsonPath("$[5].name").value("CHROME_EXTENSION"))
                .andExpect(jsonPath("$[5].enabled").value(false));
    }

    @Test
    void shouldReturnProviderHealthFromRegistry() throws Exception {
        DiscoveryProvider csvProvider = org.mockito.Mockito.mock(DiscoveryProvider.class);
        when(csvProvider.source()).thenReturn(DiscoverySource.CSV);
        when(csvProvider.health()).thenReturn(true);
        when(discoveryProviderRegistry.listProviders()).thenReturn(List.of(csvProvider));

        mockMvc.perform(get("/api/discovery/providers/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers[0].provider").value("CSV"))
                .andExpect(jsonPath("$.providers[0].status").value("UP"));
    }
}
