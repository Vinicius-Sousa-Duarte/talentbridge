package com.dunkweb.talentbridge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "keywords")
public class KeywordJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
