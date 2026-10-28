package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.CreateRecruiterRequest;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.CreateRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateRecruiterUseCaseImpl implements CreateRecruiterUseCase {

    private final RecruiterRepository recruiterRepository;
    private final RecruiterDtoMapper recruiterDtoMapper;
    private final UserProfileRepository userProfileRepository;
    private final RecruiterScoringEngine recruiterScoringEngine;

    @Override
    public RecruiterResponse execute(CreateRecruiterRequest request) {
        long startedAt = System.nanoTime();
        log.info("{} Recruiter creation started company={} position={}",
            USE_CASE,
            request.company(),
            request.position());

        int score = userProfileRepository.findActiveProfile()
                .map(profile -> recruiterScoringEngine.calculate(profile, request.headline(), request.position() + " " + request.company()))
                .orElse(0);

        Recruiter recruiter = recruiterDtoMapper.toDomain(request, score);
        Recruiter savedRecruiter = recruiterRepository.save(recruiter);
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

        log.info("{} Recruiter creation completed recruiterId={} score={} durationMs={}",
            USE_CASE,
            savedRecruiter.getId(),
            savedRecruiter.getScore(),
            durationMs);

        return recruiterDtoMapper.toResponse(savedRecruiter);
    }
}
