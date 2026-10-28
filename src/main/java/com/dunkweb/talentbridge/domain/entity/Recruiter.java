package com.dunkweb.talentbridge.domain.entity;

import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
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
public class Recruiter {

    private UUID id;
    private String name;
    private String position;
    private String company;
    private String location;
    private String linkedinUrl;
    private String headline;
    private Integer connectionsCount;
    private Integer score;
    private RecruiterStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime invitationSentAt;
    private LocalDateTime connectedAt;

    public static Recruiter create(
            String name,
            String position,
            String company,
            String location,
            String linkedinUrl,
            String headline,
            Integer connectionsCount,
            Integer score
    ) {
        validateRequired(name, "Nome");

        return Recruiter.builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .position(position == null ? null : position.trim())
                .company(company == null ? null : company.trim())
                .location(location == null ? null : location.trim())
                .linkedinUrl(linkedinUrl == null ? null : linkedinUrl.trim())
                .headline(headline == null ? null : headline.trim())
                .connectionsCount(connectionsCount)
                .score(score == null ? 0 : score)
                .status(RecruiterStatus.PENDING_REVIEW)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void approve() {
        ensureStatus(RecruiterStatus.PENDING_REVIEW, "Somente recrutadores pendentes podem ser aprovados.");
        this.status = RecruiterStatus.APPROVED;
    }

    public void registerInvite() {
        ensureStatus(RecruiterStatus.APPROVED, "Somente recrutadores aprovados podem receber convite.");
        this.status = RecruiterStatus.INVITE_SENT;
        this.invitationSentAt = LocalDateTime.now();
    }

    public void registerConnection() {
        ensureStatus(RecruiterStatus.INVITE_SENT, "Somente recrutadores com convite enviado podem virar conexao.");
        this.status = RecruiterStatus.CONNECTED;
        this.connectedAt = LocalDateTime.now();
    }

    public void dismiss() {
        if (this.status == RecruiterStatus.CONNECTED) {
            throw new BusinessException("Nao e permitido descartar um recrutador ja conectado.");
        }
        this.status = RecruiterStatus.DISMISSED;
    }

    public void recalculateScore(Integer newScore) {
        this.score = newScore == null ? 0 : Math.max(0, newScore);
    }

    private void ensureStatus(RecruiterStatus expected, String errorMessage) {
        if (this.status != expected) {
            throw new BusinessException(errorMessage);
        }
    }

    private static void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " e obrigatorio.");
        }
    }
}
