package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.CreateSkillPreferenceRequest;
import com.dunkweb.talentbridge.application.dto.UpdateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.application.mapper.UserProfileDtoMapper;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileUseCaseImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private final UserProfileDtoMapper userProfileDtoMapper = new UserProfileDtoMapper();

    @Test
    void shouldUpdateActiveProfile() {
        UserProfile existing = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.BACKEND_DEVELOPER),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Solutions Architect",
                "Focused on cloud architecture",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT, CareerObjective.CLOUD_ENGINEER),
                List.of(
                        new CreateSkillPreferenceRequest("AWS", 10),
                        new CreateSkillPreferenceRequest("Spring Boot", 8)
                )
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserProfileUseCaseImpl useCase = new UpdateUserProfileUseCaseImpl(userProfileRepository, userProfileDtoMapper);
        UserProfileResponse response = useCase.execute(request);

        assertThat(response.professionalTitle()).isEqualTo("Solutions Architect");
        assertThat(response.objectives()).containsExactlyInAnyOrder(CareerObjective.SOLUTIONS_ARCHITECT, CareerObjective.CLOUD_ENGINEER);
        assertThat(response.skills()).hasSize(2);
    }

    @Test
    void shouldThrowWhenNoActiveProfileExists() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Solutions Architect",
                "Focused on cloud architecture",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new CreateSkillPreferenceRequest("AWS", 10))
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.empty());

        UpdateUserProfileUseCaseImpl useCase = new UpdateUserProfileUseCaseImpl(userProfileRepository, userProfileDtoMapper);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Perfil profissional ativo nao encontrado");
    }
}
