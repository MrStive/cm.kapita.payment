package com.domeni.kapita.payment.domain.payment;

public enum PaymentIntentStatus {
    CREATED,
    PAYMENT_PENDING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    EXPIRED
}
