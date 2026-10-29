package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.port.input.ApproveRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.CreateRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.DeleteRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.DismissRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.GetRecruiterByIdUseCase;
import com.dunkweb.talentbridge.application.port.input.ListRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.input.RecalculateRecruiterScoreUseCase;
import com.dunkweb.talentbridge.application.port.input.RegisterConnectionUseCase;
import com.dunkweb.talentbridge.application.port.input.RegisterInviteUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(RecruiterController.class)
class RecruiterControllerRoutingTest {

    @MockBean
    private CreateRecruiterUseCase createRecruiterUseCase;

    @MockBean
    private ListRecruitersUseCase listRecruitersUseCase;

    @MockBean
    private GetRecruiterByIdUseCase getRecruiterByIdUseCase;

    @MockBean
    private ApproveRecruiterUseCase approveRecruiterUseCase;

    @MockBean
    private RegisterInviteUseCase registerInviteUseCase;

    @MockBean
    private RegisterConnectionUseCase registerConnectionUseCase;

    @MockBean
    private DismissRecruiterUseCase dismissRecruiterUseCase;

    @MockBean
    private DeleteRecruiterUseCase deleteRecruiterUseCase;

    @MockBean
    private RecalculateRecruiterScoreUseCase recalculateRecruiterScoreUseCase;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void shouldNotExposeDiscoveryEndpointInsideRecruitersController() {
        Set<String> paths = handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream())
                .collect(Collectors.toSet());

        assertThat(paths).doesNotContain("/api/recruiters/discovery");
        assertThat(paths).contains("/api/recruiters");
        assertThat(paths).contains("/api/recruiters/{id}");
        assertThat(paths).contains("/api/recruiters/recalculate-scores");
    }
}
