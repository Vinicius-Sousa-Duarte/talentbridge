package com.dunkweb.talentbridge.domain.entity;

import com.dunkweb.talentbridge.domain.exception.ValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Keyword {

    private UUID id;
    private String description;
    private LocalDateTime createdAt;

    public static Keyword create(String description) {
        validateDescription(description);

        return Keyword.builder()
                .id(UUID.randomUUID())
                .description(description.trim())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Descricao da keyword e obrigatoria.");
        }
    }
}
