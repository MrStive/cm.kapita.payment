package com.domeni.kapita.payment.service.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record PaymentNotification(
        UUID providerAttemptId,
        @Nullable String status,
        @Nullable String externalId,
        @Nullable BigDecimal amount,
        @Nullable String currency,
        Map<String, String> rawPayload) {}
