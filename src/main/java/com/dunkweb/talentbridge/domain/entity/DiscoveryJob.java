package com.dunkweb.talentbridge.domain.entity;

import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
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
public class DiscoveryJob {

    private UUID id;
    private Integer requestedProfiles;
    private Integer profilesFound;
    private Integer profilesImported;
    private Integer duplicatesIgnored;
    private Integer pagesVisited;
    private Integer queriesExecuted;
    private Long executionTimeMs;
    private DiscoveryJobStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    public static DiscoveryJob start(Integer requestedProfiles) {
        if (requestedProfiles == null || requestedProfiles < 1 || requestedProfiles > 500) {
            throw new ValidationException("requestedProfiles deve estar entre 1 e 500.");
        }

        return DiscoveryJob.builder()
                .id(UUID.randomUUID())
                .requestedProfiles(requestedProfiles)
                .profilesFound(0)
                .profilesImported(0)
                .duplicatesIgnored(0)
                .pagesVisited(0)
                .queriesExecuted(0)
                .executionTimeMs(0L)
                .status(DiscoveryJobStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static DiscoveryJob restore(
            UUID id,
            Integer requestedProfiles,
            Integer profilesFound,
            Integer profilesImported,
            Integer duplicatesIgnored,
            Integer pagesVisited,
            Integer queriesExecuted,
            Long executionTimeMs,
            DiscoveryJobStatus status,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime finishedAt
    ) {
        return DiscoveryJob.builder()
                .id(id)
                .requestedProfiles(requestedProfiles)
                .profilesFound(profilesFound)
                .profilesImported(profilesImported)
                .duplicatesIgnored(duplicatesIgnored)
                .pagesVisited(pagesVisited)
                .queriesExecuted(queriesExecuted)
                .executionTimeMs(executionTimeMs)
                .status(status)
                .errorMessage(errorMessage)
                .createdAt(createdAt)
                .finishedAt(finishedAt)
                .build();
    }

    public void complete(
            int profilesFound,
            int profilesImported,
            int duplicatesIgnored,
            int pagesVisited,
            int queriesExecuted,
            long executionTimeMs
    ) {
        this.profilesFound = Math.max(0, profilesFound);
        this.profilesImported = Math.max(0, profilesImported);
        this.duplicatesIgnored = Math.max(0, duplicatesIgnored);
        this.pagesVisited = Math.max(0, pagesVisited);
        this.queriesExecuted = Math.max(0, queriesExecuted);
        this.executionTimeMs = Math.max(0, executionTimeMs);
        this.status = DiscoveryJobStatus.COMPLETED;
        this.errorMessage = null;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage, long executionTimeMs) {
        this.status = DiscoveryJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.executionTimeMs = Math.max(0, executionTimeMs);
        this.pagesVisited = this.pagesVisited == null ? 0 : this.pagesVisited;
        this.queriesExecuted = this.queriesExecuted == null ? 0 : this.queriesExecuted;
        this.finishedAt = LocalDateTime.now();
    }
}
