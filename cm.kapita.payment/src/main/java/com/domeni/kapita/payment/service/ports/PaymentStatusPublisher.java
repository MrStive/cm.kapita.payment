package com.domeni.kapita.payment.service.ports;

import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;

public interface PaymentStatusPublisher {
    void publish(PaymentIntent paymentIntent, ProviderAttempt providerAttempt);
}
