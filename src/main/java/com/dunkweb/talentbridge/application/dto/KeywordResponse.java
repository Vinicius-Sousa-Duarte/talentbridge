package com.dunkweb.talentbridge.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record KeywordResponse(
        UUID id,
        String description,
        LocalDateTime createdAt
) {
}
