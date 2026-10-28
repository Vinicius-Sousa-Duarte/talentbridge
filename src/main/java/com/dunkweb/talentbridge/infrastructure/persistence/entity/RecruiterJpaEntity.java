package com.dunkweb.talentbridge.infrastructure.persistence.entity;

import com.dunkweb.talentbridge.domain.enums.RecruiterStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recruiters", indexes = {
        @Index(name = "idx_recruiters_status", columnList = "status"),
        @Index(name = "idx_recruiters_score", columnList = "score"),
        @Index(name = "idx_recruiters_company", columnList = "company"),
        @Index(name = "idx_recruiters_created_at", columnList = "created_at")
})
public class RecruiterJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 160)
    private String position;

    @Column(length = 160)
    private String company;

    @Column(length = 160)
    private String location;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(length = 500)
    private String headline;

    @Column(name = "connections_count")
    private Integer connectionsCount;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RecruiterStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "invitation_sent_at")
    private LocalDateTime invitationSentAt;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;
}
