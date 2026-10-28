package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.RegisterConnectionUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DOMAIN;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterConnectionUseCaseImpl implements RegisterConnectionUseCase {

    private final RecruiterRepository recruiterRepository;
    private final RecruiterDtoMapper recruiterDtoMapper;

    @Override
    public RecruiterResponse execute(UUID recruiterId) {
        long startedAt = System.nanoTime();
        log.info("{} Register connection started recruiterId={}", USE_CASE, recruiterId);

        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter nao encontrado para o id: " + recruiterId));

        log.debug("{} Recruiter status transition action=register-connection recruiterId={}", DOMAIN, recruiterId);
        recruiter.registerConnection();
        Recruiter savedRecruiter = recruiterRepository.save(recruiter);

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} Register connection completed recruiterId={} durationMs={}",
            USE_CASE,
            recruiterId,
            durationMs);

        return recruiterDtoMapper.toResponse(savedRecruiter);
    }
}
