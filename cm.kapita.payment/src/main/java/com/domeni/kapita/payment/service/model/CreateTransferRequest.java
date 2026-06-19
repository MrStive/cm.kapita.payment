package com.domeni.kapita.payment.service.model;

import java.math.BigDecimal;

public record CreateTransferRequest(
        String idempotencyKey,
        BigDecimal amount,
        String currency,
        String phoneNumber,
        String provider,
        String returnUrl,
        String userId,
        String description) {}
