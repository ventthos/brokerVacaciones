package com.tareavacaciones.brokermessagebe.models;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "payments_retry_jobs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PaymentRetryJob {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "orden_id")
    private String ordenId;

    @Column(name = "request_data", columnDefinition = "jsonb")
    private String requestData;

    @Column(name = "response_data", columnDefinition = "jsonb")
    private String responseData;

    private String action;

    private String step;

    private String status;


    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "error_message")
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