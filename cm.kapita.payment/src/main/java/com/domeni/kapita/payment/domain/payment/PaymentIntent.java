package com.domeni.kapita.payment.domain.payment;

import com.domeni.kapita.domain.core.SoftDeleteJpaEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import javax.money.MonetaryAmount;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "t_payment_intent")
@SuppressWarnings("NullAway.Init")
public class PaymentIntent extends SoftDeleteJpaEntity<PaymentIntentId> {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "c_id"))
  private PaymentIntentId id = new PaymentIntentId();

  @Column(name = "c_idempotency_key", nullable = false, unique = true)
  private String idempotencyKey;

  @Column(name = "c_request_hash", nullable = false)
  private String requestHash;

  @Column(name = "c_external_reference", nullable = false)
  private String externalReference;

  @Column(name = "c_purpose", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentPurpose purpose;

  @Column(name = "c_user_id", nullable = false)
  private String userId;

  @Column(name = "c_amount", nullable = false)
  @Convert(converter = com.domeni.kapita.payment.jpa.converter.MonetaryAmountConverter.class)
  private MonetaryAmount amount;

  @Column(name = "c_provider", nullable = false)
  private String provider;

  @Column(name = "c_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentIntentStatus status;

  @Column(name = "c_description")
  private String description;

  @Column(name = "c_paid_at")
  private Instant paidAt;

  @Column(name = "c_failed_at")
  private Instant failedAt;

  @Nullable
  @Column(name = "c_failure_reason")
  private String failureReason;

  public static PaymentIntent create(
      String idempotencyKey,
      String requestHash,
      String externalReference,
      PaymentPurpose purpose,
      String userId,
      MonetaryAmount amount,
      String provider,
      String description) {
    requireText(idempotencyKey, "idempotencyKey");
    requireText(requestHash, "requestHash");
    requireText(externalReference, "externalReference");
    requireText(userId, "userId");
    requireText(provider, "provider");
    Objects.requireNonNull(purpose, "purpose");
    Objects.requireNonNull(amount, "amount");

    PaymentIntent intent = new PaymentIntent();
    intent.idempotencyKey = idempotencyKey;
    intent.requestHash = requestHash;
    intent.externalReference = externalReference;
    intent.purpose = purpose;
    intent.userId = userId;
    intent.amount = amount;
    intent.provider = provider;
    intent.status = PaymentIntentStatus.CREATED;
    intent.description = description;
    return intent;
  }

  public boolean hasRequestHash(String expectedHash) {
    return Objects.equals(requestHash, expectedHash);
  }

  public boolean isFinal() {
    return status == PaymentIntentStatus.SUCCEEDED
        || status == PaymentIntentStatus.FAILED
        || status == PaymentIntentStatus.CANCELLED
        || status == PaymentIntentStatus.EXPIRED;
  }

  public void markPaymentPending() {
    if (status == PaymentIntentStatus.PAYMENT_PENDING) {
      return;
    }
    requireStatus(PaymentIntentStatus.CREATED);
    status = PaymentIntentStatus.PAYMENT_PENDING;
  }

  public void markSucceeded() {
    if (status == PaymentIntentStatus.SUCCEEDED) {
      return;
    }
    requireStatus(PaymentIntentStatus.PAYMENT_PENDING);
    status = PaymentIntentStatus.SUCCEEDED;
    paidAt = Instant.now();
    failureReason = null;
  }

  public void markFailed(String reason) {
    if (status == PaymentIntentStatus.FAILED) {
      return;
    }
    requireAnyStatus(PaymentIntentStatus.CREATED, PaymentIntentStatus.PAYMENT_PENDING);
    status = PaymentIntentStatus.FAILED;
    failedAt = Instant.now();
    failureReason = reason;
  }

  public void markCancelled(String reason) {
    if (status == PaymentIntentStatus.CANCELLED) {
      return;
    }
    requireAnyStatus(PaymentIntentStatus.CREATED, PaymentIntentStatus.PAYMENT_PENDING);
    status = PaymentIntentStatus.CANCELLED;
    failedAt = Instant.now();
    failureReason = reason;
  }

  public void markExpired(String reason) {
    if (status == PaymentIntentStatus.EXPIRED) {
      return;
    }
    requireAnyStatus(PaymentIntentStatus.CREATED, PaymentIntentStatus.PAYMENT_PENDING);
    status = PaymentIntentStatus.EXPIRED;
    failedAt = Instant.now();
    failureReason = reason;
  }

  private void requireStatus(PaymentIntentStatus expectedStatus) {
    if (status != expectedStatus) {
      throw new IllegalStateException(
          "Invalid payment intent status transition from " + status + " to " + expectedStatus);
    }
  }

  private void requireAnyStatus(PaymentIntentStatus... expectedStatuses) {
    for (PaymentIntentStatus expectedStatus : expectedStatuses) {
      if (status == expectedStatus) {
        return;
      }
    }
    throw new IllegalStateException("Invalid payment intent status transition from " + status);
  }

  private static void requireText(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " is required");
    }
  }

  @Override
  public void setId(PaymentIntentId paymentIntentId) {
    this.id = paymentIntentId;
  }
}
