package com.domeni.kapita.payment.service.events.model;

import java.time.Instant;

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
        String failureReason,
        Instant occurredAt) {}
