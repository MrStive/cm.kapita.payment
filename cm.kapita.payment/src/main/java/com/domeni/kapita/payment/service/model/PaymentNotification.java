package com.domeni.kapita.payment.service.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record PaymentNotification(
        UUID providerAttemptId,
        String status,
        String externalId,
        BigDecimal amount,
        String currency,
        Map<String, String> rawPayload) {}
