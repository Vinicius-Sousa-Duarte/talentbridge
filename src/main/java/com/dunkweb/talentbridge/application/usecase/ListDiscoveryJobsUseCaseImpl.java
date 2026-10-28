package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.application.mapper.DiscoveryJobDtoMapper;
import com.dunkweb.talentbridge.application.port.input.ListDiscoveryJobsUseCase;
import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListDiscoveryJobsUseCaseImpl implements ListDiscoveryJobsUseCase {

    private final DiscoveryJobRepository discoveryJobRepository;
    private final DiscoveryJobDtoMapper discoveryJobDtoMapper;

    @Override
    public List<DiscoveryJobResponse> execute() {
        return discoveryJobRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(discoveryJobDtoMapper::toResponse)
                .toList();
    }
}
