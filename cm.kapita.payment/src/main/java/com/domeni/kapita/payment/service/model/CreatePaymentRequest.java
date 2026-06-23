package com.domeni.kapita.payment.service.model;

public record CreatePaymentRequest(
        String idempotencyKey,
        String externalReference,
        String purpose,
        Money money,
        String phoneNumber,
        String provider,
        String returnUrl,
        String userId,
        String description) {}
