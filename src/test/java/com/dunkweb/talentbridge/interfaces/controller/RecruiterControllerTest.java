package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.dto.RecruiterPageResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.port.input.ApproveRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.CreateRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.DeleteRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.DismissRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.GetRecruiterByIdUseCase;
import com.dunkweb.talentbridge.application.port.input.ListRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.input.RecalculateRecruiterScoreUseCase;
import com.dunkweb.talentbridge.application.port.input.RegisterConnectionUseCase;
import com.dunkweb.talentbridge.application.port.input.RegisterInviteUseCase;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecruiterController.class)
class RecruiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void shouldListRecruitersWithDefaultPaginationAndSort() throws Exception {
        RecruiterPageResponse page = new RecruiterPageResponse(
                List.of(recruiter("Ana", 20)),
                0,
                20,
                1,
                1,
                "score,desc;createdAt,desc"
        );
        when(listRecruitersUseCase.execute(eq(null), eq(null), eq(null))).thenReturn(page);

        mockMvc.perform(get("/api/recruiters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ana"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.sort").value("score,desc;createdAt,desc"));
    }

    @Test
    void shouldListRecruitersWithExplicitPageSizeAndSort() throws Exception {
        RecruiterPageResponse page = new RecruiterPageResponse(
                List.of(recruiter("Bruno", 15)),
                1,
                5,
                7,
                2,
                "createdAt,asc"
        );
        when(listRecruitersUseCase.execute(eq(1), eq(5), eq("createdAt,asc"))).thenReturn(page);

        mockMvc.perform(get("/api/recruiters")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Bruno"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(7));
    }

    private RecruiterResponse recruiter(String name, int score) {
        return new RecruiterResponse(
                UUID.randomUUID(),
                name,
                "Tech Recruiter",
                "Acme",
                "Sao Paulo",
                "https://linkedin.com/in/" + name.toLowerCase(),
                "Hiring",
                null,
                score,
                RecruiterStatus.PENDING_REVIEW,
                LocalDateTime.now(),
                null,
                null
        );
    }
}
