package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.DismissRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.entity.Recruiter;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DismissRecruiterUseCaseImpl implements DismissRecruiterUseCase {

    private final RecruiterRepository recruiterRepository;
    private final RecruiterDtoMapper recruiterDtoMapper;

    @Override
    public RecruiterResponse execute(UUID recruiterId) {
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter nao encontrado para o id: " + recruiterId));

        recruiter.dismiss();
        Recruiter savedRecruiter = recruiterRepository.save(recruiter);
        return recruiterDtoMapper.toResponse(savedRecruiter);
    }
}
