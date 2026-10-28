package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecalculateRecruiterScoresResponse;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecalculateRecruiterScoreUseCaseImplTest {

    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RecruiterScoringEngine recruiterScoringEngine;

    @Test
    void shouldRecalculateAllRecruiters() {
        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(
                        new UserProfile.SkillInput("Java", 10),
                        new UserProfile.SkillInput("AWS", 10)
                )
        );

        Recruiter recruiterA = Recruiter.create(
                "Camila",
                "Tech Recruiter",
                "SkyTech",
                "Sao Paulo",
                "https://linkedin.com/in/camila",
                "Hiring Java developers",
                300,
                0
        );

        Recruiter recruiterB = Recruiter.create(
                "John",
                "Talent Acquisition",
                "CloudBridge",
                "Lisbon",
                "https://linkedin.com/in/john",
                "Hiring AWS engineers",
                400,
                0
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(profile));
        when(recruiterRepository.findAll()).thenReturn(List.of(recruiterA, recruiterB));
        when(recruiterScoringEngine.calculate(any(UserProfile.class), any(String.class), any(String.class), any()))
                .thenReturn(10)
                .thenReturn(10);
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecalculateRecruiterScoreUseCaseImpl useCase = new RecalculateRecruiterScoreUseCaseImpl(
                recruiterRepository,
                userProfileRepository,
                recruiterScoringEngine
        );

        RecalculateRecruiterScoresResponse response = useCase.execute();

        assertThat(response.recruitersUpdated()).isEqualTo(2);
        assertThat(recruiterA.getScore()).isEqualTo(10);
        assertThat(recruiterB.getScore()).isEqualTo(10);
        verify(recruiterRepository, times(2)).save(any(Recruiter.class));
    }

    @Test
    void shouldFailWhenNoActiveProfileExists() {
        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.empty());

        RecalculateRecruiterScoreUseCaseImpl useCase = new RecalculateRecruiterScoreUseCaseImpl(
                recruiterRepository,
                userProfileRepository,
                recruiterScoringEngine
        );

        assertThatThrownBy(useCase::execute)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Perfil profissional ativo nao encontrado");
    }
}
