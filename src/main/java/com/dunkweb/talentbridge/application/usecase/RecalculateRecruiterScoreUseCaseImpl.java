package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecalculateRecruiterScoresResponse;
import com.dunkweb.talentbridge.application.port.input.RecalculateRecruiterScoreUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.application.port.output.UserProfileRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import com.dunkweb.talentbridge.domain.service.RecruiterScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.USE_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecalculateRecruiterScoreUseCaseImpl implements RecalculateRecruiterScoreUseCase {

    private final RecruiterRepository recruiterRepository;
    private final UserProfileRepository userProfileRepository;
    private final RecruiterScoringEngine recruiterScoringEngine;

    @Override
    public RecalculateRecruiterScoresResponse execute() {
        long startedAt = System.nanoTime();
        log.info("{} Score recalculation started", USE_CASE);

        UserProfile activeProfile = userProfileRepository.findActiveProfile()
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional ativo nao encontrado."));

        List<Recruiter> recruiters = recruiterRepository.findAll();
        recruiters.forEach(recruiter -> {
            int score = recruiterScoringEngine.calculate(
                    activeProfile,
                    recruiter.getHeadline(),
                recruiter.getPosition() + " " + recruiter.getCompany(),
                recruiter.getId()
            );
            recruiter.recalculateScore(score);
            recruiterRepository.save(recruiter);
            log.debug("{} Score recalculated recruiterId={} newScore={}",
                USE_CASE,
                recruiter.getId(),
                score);
        });

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} Score recalculation finished processedCount={} durationMs={}",
            USE_CASE,
            recruiters.size(),
            durationMs);

        return new RecalculateRecruiterScoresResponse(recruiters.size());
    }
}
