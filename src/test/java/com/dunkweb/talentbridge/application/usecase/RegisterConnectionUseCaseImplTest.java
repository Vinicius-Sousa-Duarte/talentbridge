package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterConnectionUseCaseImplTest {

    @Mock
    private RecruiterRepository recruiterRepository;

    private final RecruiterDtoMapper recruiterDtoMapper = new RecruiterDtoMapper();

    @Test
    void shouldRegisterConnectionAndSetConnectionDate() {
        UUID recruiterId = UUID.randomUUID();
        Recruiter recruiter = Recruiter.create(
                "Ana Silva",
                "Tech Recruiter",
                "Tech Co",
                "Brasil",
                "https://linkedin.com/in/ana",
                "Hiring Java backend",
                500,
                55
        );
        recruiter.approve();
        recruiter.registerInvite();

        when(recruiterRepository.findById(recruiterId)).thenReturn(Optional.of(recruiter));
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterConnectionUseCaseImpl useCase = new RegisterConnectionUseCaseImpl(recruiterRepository, recruiterDtoMapper);
        RecruiterResponse response = useCase.execute(recruiterId);

        assertThat(response.status()).isEqualTo(RecruiterStatus.CONNECTED);
        assertThat(response.connectedAt()).isNotNull();
        verify(recruiterRepository).save(recruiter);
    }
}
