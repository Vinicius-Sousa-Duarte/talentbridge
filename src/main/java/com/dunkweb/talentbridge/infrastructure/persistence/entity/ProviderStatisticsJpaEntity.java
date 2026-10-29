package com.dunkweb.talentbridge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "provider_statistics", indexes = {
        @Index(name = "idx_provider_statistics_provider", columnList = "provider_name", unique = true),
        @Index(name = "idx_provider_statistics_last_execution", columnList = "last_execution_at")
})
public class ProviderStatisticsJpaEntity {

    @Id
    private UUID id;

    @Column(name = "provider_name", nullable = false, length = 80, unique = true)
    private String providerName;

    @Column(nullable = false)
    private Long executions;

    @Column(name = "successful_executions", nullable = false)
    private Long successfulExecutions;

    @Column(name = "failed_executions", nullable = false)
    private Long failedExecutions;

    @Column(name = "candidates_found", nullable = false)
    private Long candidatesFound;

    @Column(name = "candidates_imported", nullable = false)
    private Long candidatesImported;

    @Column(name = "duplicates_ignored", nullable = false)
    private Long duplicatesIgnored;

    @Column(name = "total_execution_time_ms", nullable = false)
    private Long totalExecutionTimeMs;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "score_samples", nullable = false)
    private Long scoreSamples;

    @Column(name = "average_execution_time_ms", nullable = false)
    private Double averageExecutionTimeMs;

    @Column(name = "average_score", nullable = false)
    private Double averageScore;

    @Column(name = "last_execution_at")
    private LocalDateTime lastExecutionAt;
}
