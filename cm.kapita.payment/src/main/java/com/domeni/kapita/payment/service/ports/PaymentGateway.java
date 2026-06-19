package com.domeni.kapita.payment.service.ports;

import java.util.Optional;

public interface PaymentGateway {
    Optional<String> initiatePayment(PaymentInitiationRequest request);
}
