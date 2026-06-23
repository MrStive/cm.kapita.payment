package com.domeni.kapita.payment.service.ports;

import com.domeni.kapita.payment.domain.payment.PaymentIntentId;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttemptId;
import javax.money.MonetaryAmount;
import lombok.Builder;

@Builder
public record PaymentInitiationRequest(
        PaymentIntentId paymentIntentId,
        ProviderAttemptId providerAttemptId,
        MonetaryAmount amount,
        String phoneNumber,
        String provider,
        String returnUrl,
        String description) {}
