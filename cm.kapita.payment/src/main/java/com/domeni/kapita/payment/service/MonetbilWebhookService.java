package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.provider_transaction.ProviderTransaction;
import com.domeni.kapita.payment.domain.provider_transaction.ProviderTransactionStatus;
import com.domeni.kapita.payment.domain.transaction.Transaction;
import com.domeni.kapita.payment.domain.transaction.TransactionStatus;
import com.domeni.kapita.payment.repositories.AccountRepository;
import com.domeni.kapita.payment.repositories.ProviderTransactionRepository;
import com.domeni.kapita.payment.repositories.TransactionRepository;
import com.domeni.kapita.payment.service.events.kafka.PaymentStatusEventProducer;
import com.domeni.kapita.payment.service.events.model.PaymentStatusEvent;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonetbilWebhookService {
    private final ProviderTransactionRepository providerTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final PaymentStatusEventProducer paymentStatusEventProducer;

    @Transactional
    public void handleNotification(UUID providerTransactionId, Map<String, String> payload) {
        // ... (existing signature verification TODO)

        ProviderTransaction providerTransaction =
                providerTransactionRepository
                        .findById(
                                new com.domeni.kapita.payment.domain.provider_transaction
                                        .ProviderTransactionId(providerTransactionId))
                        .orElseThrow();
        Transaction transaction =
                transactionRepository
                        .findById(
                                new com.domeni.kapita.payment.domain.transaction.TransactionId(
                                        providerTransaction.getTransactionId().toUUID()))
                        .orElseThrow();

        String status = payload.get("status");

        if ("success".equalsIgnoreCase(status)) {
            providerTransaction.setStatus(ProviderTransactionStatus.SUCCESS);
            transaction.setStatus(TransactionStatus.COMPLETED);
            paymentStatusEventProducer.send(
                    new PaymentStatusEvent(
                            transaction.getId().getValue(),
                            "SUCCESS",
                            new com.domeni.kapita.payment.service.events.model.MoneyDTO(
                                    transaction.getAmount().getNumber().numberValue(java.math.BigDecimal.class),
                                    transaction.getAmount().getCurrency().getCurrencyCode())));
        } else {
            providerTransaction.setStatus(ProviderTransactionStatus.FAILED);
            transaction.setStatus(TransactionStatus.FAILED);
            paymentStatusEventProducer.send(
                    new PaymentStatusEvent(
                            transaction.getId().getValue(),
                            "FAILED",
                            new com.domeni.kapita.payment.service.events.model.MoneyDTO(
                                    transaction.getAmount().getNumber().numberValue(java.math.BigDecimal.class),
                                    transaction.getAmount().getCurrency().getCurrencyCode())));
        }

        providerTransactionRepository.save(providerTransaction);
        transactionRepository.save(transaction);
    }
}
