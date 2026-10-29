package com.dunkweb.talentbridge.application.usecase;

import com.dunkweb.talentbridge.application.dto.RecruiterPageResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.mapper.RecruiterDtoMapper;
import com.dunkweb.talentbridge.application.port.input.ListRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.output.RecruiterPage;
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
    public RecruiterPageResponse execute(Integer page, Integer size, String sort) {
        int safePage = page == null ? 0 : Math.max(0, page);
        int safeSize = size == null ? 20 : Math.max(1, size);
        String safeSort = sort == null || sort.isBlank() ? "score,desc;createdAt,desc" : sort;

        RecruiterPage recruiterPage = recruiterRepository.findPage(safePage, safeSize, safeSort);

        List<RecruiterResponse> content = recruiterPage.content().stream()
                .map(recruiterDtoMapper::toResponse)
                .toList();

        return new RecruiterPageResponse(
                content,
                recruiterPage.page(),
                recruiterPage.size(),
                recruiterPage.totalElements(),
                recruiterPage.totalPages(),
                recruiterPage.sort()
        );
    }
}
