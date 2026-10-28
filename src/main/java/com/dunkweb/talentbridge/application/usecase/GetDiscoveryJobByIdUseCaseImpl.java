package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.application.mapper.DiscoveryJobDtoMapper;
import com.dunkweb.talentbridge.application.port.input.GetDiscoveryJobByIdUseCase;
import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDiscoveryJobByIdUseCaseImpl implements GetDiscoveryJobByIdUseCase {

    private final DiscoveryJobRepository discoveryJobRepository;
    private final DiscoveryJobDtoMapper discoveryJobDtoMapper;

    @Override
    public DiscoveryJobResponse execute(UUID jobId) {
        return discoveryJobRepository.findById(jobId)
                .map(discoveryJobDtoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Discovery job nao encontrado para o id: " + jobId));
    }
}
