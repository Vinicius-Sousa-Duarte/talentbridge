package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.application.mapper.UserProfileDtoMapper;
import com.dunkweb.talentbridge.application.port.input.GetUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserProfileUseCaseImpl implements GetUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileDtoMapper userProfileDtoMapper;

    @Override
    public UserProfileResponse execute() {
        return userProfileRepository.findActiveProfile()
                .map(userProfileDtoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional ativo nao encontrado."));
    }
}
