package com.dunkweb.talentbridge.infrastructure.persistence.entity;

import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
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
@Table(name = "discovery_jobs", indexes = {
        @Index(name = "idx_discovery_jobs_status", columnList = "status"),
        @Index(name = "idx_discovery_jobs_created_at", columnList = "created_at")
})
public class DiscoveryJobJpaEntity {

    @Id
    private UUID id;

    @Column(name = "requested_profiles", nullable = false)
    private Integer requestedProfiles;

    @Column(name = "profiles_found", nullable = false)
    private Integer profilesFound;

    @Column(name = "profiles_imported", nullable = false)
    private Integer profilesImported;

    @Column(name = "duplicates_ignored", nullable = false)
    private Integer duplicatesIgnored;

    @Column(name = "pages_visited", nullable = false)
    private Integer pagesVisited;

    @Column(name = "queries_executed", nullable = false)
    private Integer queriesExecuted;

    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DiscoveryJobStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}
