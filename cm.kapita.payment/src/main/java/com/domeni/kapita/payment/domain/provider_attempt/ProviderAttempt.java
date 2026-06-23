package com.domeni.kapita.payment.domain.provider_attempt;

import com.domeni.kapita.domain.core.SoftDeleteJpaEntity;
import com.domeni.kapita.payment.domain.payment.PaymentIntentId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_provider_attempt")
@SuppressWarnings({"all", "NullAway.Init"})
public class ProviderAttempt extends SoftDeleteJpaEntity<ProviderAttemptId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "c_id"))
    private ProviderAttemptId id = new ProviderAttemptId();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "c_payment_intent_id"))
    private PaymentIntentId paymentIntentId;

    @Column(name = "c_provider", nullable = false)
    private String provider;

    @Column(name = "c_provider_reference")
    private String providerReference;

    @Column(name = "c_payment_url")
    private String paymentUrl;

    @Column(name = "c_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderAttemptStatus status;

    @Column(name = "c_raw_response")
    private String rawResponse;

    @Column(name = "c_raw_webhook")
    private String rawWebhook;

    @Column(name = "c_failure_reason")
    private String failureReason;

    @Column(name = "c_completed_at")
    private Instant completedAt;

    public static ProviderAttempt create(PaymentIntentId paymentIntentId, String provider) {
        Objects.requireNonNull(paymentIntentId, "paymentIntentId");
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider is required");
        }

        ProviderAttempt attempt = new ProviderAttempt();
        attempt.paymentIntentId = paymentIntentId;
        attempt.provider = provider;
        attempt.status = ProviderAttemptStatus.CREATED;
        return attempt;
    }

    public boolean isFinal() {
        return status == ProviderAttemptStatus.SUCCEEDED
                || status == ProviderAttemptStatus.FAILED
                || status == ProviderAttemptStatus.CANCELLED
                || status == ProviderAttemptStatus.EXPIRED;
    }

    public void markPending(String providerReference, String paymentUrl, String rawResponse) {
        if (status == ProviderAttemptStatus.PENDING) {
            return;
        }
        requireStatus(ProviderAttemptStatus.CREATED);
        this.providerReference = providerReference;
        this.paymentUrl = paymentUrl;
        this.rawResponse = rawResponse;
        this.status = ProviderAttemptStatus.PENDING;
    }

    public void markSucceeded(String providerReference, String rawWebhook) {
        if (status == ProviderAttemptStatus.SUCCEEDED) {
            return;
        }
        requireStatus(ProviderAttemptStatus.PENDING);
        if (providerReference != null && !providerReference.isBlank()) {
            this.providerReference = providerReference;
        }
        this.rawWebhook = rawWebhook;
        this.status = ProviderAttemptStatus.SUCCEEDED;
        this.completedAt = Instant.now();
        this.failureReason = null;
    }

    public void markFailed(String reason, String rawPayload) {
        if (status == ProviderAttemptStatus.FAILED) {
            return;
        }
        requireAnyStatus(ProviderAttemptStatus.CREATED, ProviderAttemptStatus.PENDING);
        this.failureReason = reason;
        this.rawWebhook = rawPayload;
        this.status = ProviderAttemptStatus.FAILED;
        this.completedAt = Instant.now();
    }

    public void markCancelled(String reason, String rawPayload) {
        if (status == ProviderAttemptStatus.CANCELLED) {
            return;
        }
        requireAnyStatus(ProviderAttemptStatus.CREATED, ProviderAttemptStatus.PENDING);
        this.failureReason = reason;
        this.rawWebhook = rawPayload;
        this.status = ProviderAttemptStatus.CANCELLED;
        this.completedAt = Instant.now();
    }

    public void markExpired(String reason, String rawPayload) {
        if (status == ProviderAttemptStatus.EXPIRED) {
            return;
        }
        requireAnyStatus(ProviderAttemptStatus.CREATED, ProviderAttemptStatus.PENDING);
        this.failureReason = reason;
        this.rawWebhook = rawPayload;
        this.status = ProviderAttemptStatus.EXPIRED;
        this.completedAt = Instant.now();
    }

    private void requireStatus(ProviderAttemptStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                    "Invalid provider attempt status transition from "
                            + status
                            + " to "
                            + expectedStatus);
        }
    }

    private void requireAnyStatus(ProviderAttemptStatus... expectedStatuses) {
        for (ProviderAttemptStatus expectedStatus : expectedStatuses) {
            if (status == expectedStatus) {
                return;
            }
        }
        throw new IllegalStateException(
                "Invalid provider attempt status transition from " + status);
    }
}
