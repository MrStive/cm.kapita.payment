package com.domeni.kapita.payment.service.model;

import java.util.UUID;

public record PaymentNotification(
    UUID providerTransactionId,
    String status,
    String externalId,
    String amount,
    String currency
) {}
