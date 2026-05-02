package com.tareavacaciones.brokermessagebe.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "retry_pagos_deuda")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDebtRetryJob implements RetryJob {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "orden_id")
    private String ordenId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_data")
    private String requestData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "step_status")
    private String stepStatus; // JSON string mapping step names to status/results

    private String status; // SCHEDULED, COMPLETED, FAILED

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "next_run_at")
    private OffsetDateTime nextRunAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
