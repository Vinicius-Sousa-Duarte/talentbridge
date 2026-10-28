package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.port.input.DeleteRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteRecruiterUseCaseImpl implements DeleteRecruiterUseCase {

    private final RecruiterRepository recruiterRepository;

    @Override
    public void execute(UUID recruiterId) {
        if (!recruiterRepository.existsById(recruiterId)) {
            throw new ResourceNotFoundException("Recruiter nao encontrado para o id: " + recruiterId);
        }
        recruiterRepository.deleteById(recruiterId);
    }
}
