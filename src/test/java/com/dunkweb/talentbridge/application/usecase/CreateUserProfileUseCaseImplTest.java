package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.CreateSkillPreferenceRequest;
import com.dunkweb.talentbridge.application.dto.CreateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.application.mapper.UserProfileDtoMapper;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserProfileUseCaseImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private final UserProfileDtoMapper userProfileDtoMapper = new UserProfileDtoMapper();

    @Test
    void shouldCreateProfileWhenNoActiveProfileExists() {
        CreateUserProfileRequest request = new CreateUserProfileRequest(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(
                        new CreateSkillPreferenceRequest("Java", 10),
                        new CreateSkillPreferenceRequest("AWS", 10)
                )
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateUserProfileUseCaseImpl useCase = new CreateUserProfileUseCaseImpl(userProfileRepository, userProfileDtoMapper);
        UserProfileResponse response = useCase.execute(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.professionalTitle()).isEqualTo("Senior Backend Developer");
        assertThat(response.skills()).hasSize(2);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void shouldFailWhenActiveProfileAlreadyExists() {
        CreateUserProfileRequest request = new CreateUserProfileRequest(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new CreateSkillPreferenceRequest("Java", 10))
        );

        UserProfile existing = UserProfile.create(
                "Existing",
                "Existing summary",
                Set.of(CareerObjective.SOFTWARE_ENGINEER),
                List.of(new UserProfile.SkillInput("Java", 5))
        );

        when(userProfileRepository.findActiveProfile()).thenReturn(Optional.of(existing));

        CreateUserProfileUseCaseImpl useCase = new CreateUserProfileUseCaseImpl(userProfileRepository, userProfileDtoMapper);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe um perfil profissional ativo");

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }
}
