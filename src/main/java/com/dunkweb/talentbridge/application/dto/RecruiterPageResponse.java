package com.dunkweb.talentbridge.application.dto;

import java.util.List;

public record RecruiterPageResponse(
        List<RecruiterResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort
) {
}
