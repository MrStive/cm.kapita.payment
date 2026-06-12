package com.domeni.kapita.payment.service.model;

public record CreateTransferRequest(
        String idempotencyKey,
        String amount,
        String currency,
        String phoneNumber,
        String provider,
        String returnUrl,
        String description) {}
