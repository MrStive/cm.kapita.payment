package com.domeni.kapita.payment.service.model;

public record CreatedPayment(
        String paymentId, String externalReference, String status, String paymentUrl) {
    public static CreatedPayment of(
            String paymentId, String externalReference, String status, String paymentUrl) {
        return new CreatedPayment(paymentId, externalReference, status, paymentUrl);
    }
}
