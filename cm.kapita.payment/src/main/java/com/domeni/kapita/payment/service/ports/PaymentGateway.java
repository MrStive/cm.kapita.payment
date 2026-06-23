package com.domeni.kapita.payment.service.ports;

public interface PaymentGateway {
    PaymentGatewayResult initiatePayment(PaymentInitiationRequest request);
}
