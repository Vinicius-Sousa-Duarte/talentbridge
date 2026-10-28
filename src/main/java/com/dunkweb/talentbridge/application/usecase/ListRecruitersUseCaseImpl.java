package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.ListRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListRecruitersUseCaseImpl implements ListRecruitersUseCase {

    private final RecruiterRepository recruiterRepository;
    private final RecruiterDtoMapper recruiterDtoMapper;

    @Override
    public List<RecruiterResponse> execute() {
        return recruiterRepository.findAll().stream()
                .map(recruiterDtoMapper::toResponse)
                .toList();
    }
}
