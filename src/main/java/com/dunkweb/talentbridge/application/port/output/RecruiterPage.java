package com.dunkweb.talentbridge.application.port.output;

import com.dunkweb.talentbridge.domain.entity.Recruiter;

import java.util.List;

public record RecruiterPage(
        List<Recruiter> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort
) {
}
