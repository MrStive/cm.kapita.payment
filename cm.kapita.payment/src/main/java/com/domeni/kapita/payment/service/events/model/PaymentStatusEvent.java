package com.domeni.kapita.payment.service.events.model;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record PaymentStatusEvent(
    String paymentId,
    String externalReference,
    String purpose,
    String userId,
    String status,
    MoneyDTO money,
    String provider,
    String providerAttemptId,
    String providerReference,
    @Nullable String failureReason,
    Instant occurredAt) {}
