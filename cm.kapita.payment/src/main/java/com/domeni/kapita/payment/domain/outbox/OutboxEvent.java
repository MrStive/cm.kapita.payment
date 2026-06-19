package com.domeni.kapita.payment.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_outbox_event")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(name = "c_id")
    private String id;

    @Column(name = "c_aggregate_id")
    private String aggregateId;

    @Column(name = "c_type")
    private String type;

    @Column(name = "c_payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "c_status")
    private String status;

    @Column(name = "c_created_at")
    private LocalDateTime createdAt;

    @Column(name = "c_processed_at")
    private LocalDateTime processedAt;

    public static OutboxEvent of(String aggregateId, String type, String payload) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .aggregateId(aggregateId)
                .type(type)
                .payload(payload)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markAsProcessed() {
        this.status = "PROCESSED";
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = "FAILED";
    }
}
