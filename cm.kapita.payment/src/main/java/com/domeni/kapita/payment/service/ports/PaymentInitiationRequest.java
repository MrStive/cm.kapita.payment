package com.domeni.kapita.payment.service.ports;

import com.domeni.kapita.payment.domain.provider_transaction.ProviderTransactionId;
import com.domeni.kapita.payment.domain.transaction.TransactionId;
import javax.money.MonetaryAmount;
import lombok.Builder;

@Builder
public record PaymentInitiationRequest(
        TransactionId transactionId,
        ProviderTransactionId providerTransactionId,
        MonetaryAmount amount,
        String phoneNumber,
        String provider,
        String description
) {}
