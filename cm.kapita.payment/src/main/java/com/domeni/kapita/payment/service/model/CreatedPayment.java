package com.domeni.kapita.payment.service.model;

import org.jspecify.annotations.Nullable;

public record CreatedPayment(
        String paymentId, String externalReference, String status, @Nullable String paymentUrl) {
    public static CreatedPayment of(
            String paymentId,
            String externalReference,
            String status,
            @Nullable String paymentUrl) {
        return new CreatedPayment(paymentId, externalReference, status, paymentUrl);
    }
}
