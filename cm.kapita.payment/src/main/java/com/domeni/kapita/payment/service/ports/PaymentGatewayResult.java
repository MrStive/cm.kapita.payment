package com.domeni.kapita.payment.service.ports;

public record PaymentGatewayResult(
    String providerReference, String paymentUrl, String rawResponse) {}
