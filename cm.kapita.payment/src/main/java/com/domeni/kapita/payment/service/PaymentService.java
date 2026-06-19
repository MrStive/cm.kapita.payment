package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.account.Account;
import com.domeni.kapita.payment.domain.account.AccountType;
import com.domeni.kapita.payment.domain.exception.PaymentProviderException;
import com.domeni.kapita.payment.domain.exception.ResourceNotFoundException;
import com.domeni.kapita.payment.domain.provider_transaction.*;
import com.domeni.kapita.payment.domain.transaction.*;
import com.domeni.kapita.payment.repositories.AccountRepository;
import com.domeni.kapita.payment.repositories.ProviderTransactionRepository;
import com.domeni.kapita.payment.repositories.TransactionRepository;
import com.domeni.kapita.payment.service.model.CreateTransferRequest;
import com.domeni.kapita.payment.service.model.CreatedTransferRequest;
import com.domeni.kapita.payment.service.model.PaymentNotification;
import com.domeni.kapita.payment.service.ports.PaymentGateway;
import com.domeni.kapita.payment.service.ports.PaymentInitiationRequest;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import java.util.Optional;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"all", "NullAway.Init"})
public class PaymentService {
    private final TransactionRepository transactionRepository;
    private final ProviderTransactionRepository providerTransactionRepository;
    private final AccountRepository accountRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentStatusPublisher paymentStatusPublisher;

    @Transactional
    public CreatedTransferRequest processDeposit(CreateTransferRequest request) {
        return transactionRepository
                .findByIdempotencyKey(request.idempotencyKey())
                .map(this::mapToCreatedTransfer)
                .orElseGet(() -> createAndInitiateTransaction(request));
    }

    @Transactional
    public void handlePaymentNotification(PaymentNotification notification) {
        ProviderTransaction providerTransaction =
                providerTransactionRepository
                        .findById(new ProviderTransactionId(notification.providerTransactionId()))
                        .orElseThrow(() -> new ResourceNotFoundException("ProviderTransaction", notification.providerTransactionId().toString()));

        Transaction transaction =
                transactionRepository
                        .findById(new TransactionId(providerTransaction.getTransactionId().toUUID()))
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction", providerTransaction.getTransactionId().toUUID().toString()));

        if ("success".equalsIgnoreCase(notification.status())) {
            providerTransaction.setStatus(ProviderTransactionStatus.SUCCESS);
            providerTransaction.setExternalId(notification.externalId());
            transaction.complete();
        } else {
            providerTransaction.setStatus(ProviderTransactionStatus.FAILED);
            transaction.fail();
        }

        providerTransactionRepository.save(providerTransaction);
        transactionRepository.save(transaction);
        paymentStatusPublisher.publish(transaction);
    }

    private CreatedTransferRequest mapToCreatedTransfer(Transaction transaction) {
        String paymentUrl = providerTransactionRepository
                .findByTransactionId(transaction.getId())
                .map(ProviderTransaction::getPaymentUrl)
                .orElse(null);
        return CreatedTransferRequest.of(
                transaction.getId().getValue(), paymentUrl, transaction);
    }

    private CreatedTransferRequest createAndInitiateTransaction(CreateTransferRequest request) {
        Account platformAccount = accountRepository.findByType(AccountType.INTERNAL)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "INTERNAL"));

        Account userAccount = accountRepository.findByOwnerId(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "OwnerId: " + request.userId()));

        Transaction transaction = Transaction.builder()
                .idempotencyKey(request.idempotencyKey())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .fromAccountId(platformAccount.getId())
                .toAccountId(userAccount.getId())
                .amount(Monetary.getDefaultAmountFactory()
                        .setNumber(request.amount())
                        .setCurrency(request.currency())
                        .create())
                .reason("Deposit via " + request.provider())
                .description(request.description())
                .build();

        transactionRepository.save(transaction);

        ProviderTransactionId providerTransactionId = new ProviderTransactionId();

        try {
            PaymentInitiationRequest initiationRequest = PaymentInitiationRequest.builder()
                    .transactionId(transaction.getId())
                    .providerTransactionId(providerTransactionId)
                    .amount(transaction.getAmount())
                    .phoneNumber(request.phoneNumber())
                    .provider(request.provider())
                    .description(transaction.getDescription())
                    .build();

            Optional<String> paymentUrl = paymentGateway.initiatePayment(initiationRequest);

            ProviderTransaction providerTransaction = ProviderTransaction.builder()
                    .id(providerTransactionId)
                    .transactionId(transaction.getId())
                    .provider(request.provider())
                    .paymentUrl(paymentUrl.orElse(null))
                    .status(ProviderTransactionStatus.PENDING)
                    .build();
            providerTransactionRepository.save(providerTransaction);

            return CreatedTransferRequest.of(transaction.getId().getValue(), providerTransaction.getPaymentUrl(), transaction);
        } catch (Exception e) {
            transaction.fail();
            transactionRepository.save(transaction);
            paymentStatusPublisher.publish(transaction);
            throw new PaymentProviderException(request.provider(), "Failed to initiate payment", e);
        }
    }
}
