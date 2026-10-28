package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.port.input.DeleteUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserProfileUseCaseImpl implements DeleteUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void execute() {
        UserProfile activeProfile = userProfileRepository.findActiveProfile()
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional ativo nao encontrado."));

        userProfileRepository.deleteById(activeProfile.getId());
    }
}
