package com.domeni.kapita.payment.service;

import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentRequestDto;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto;
import com.domeni.kapita.payment.domain.account.Account;
import com.domeni.kapita.payment.domain.account.AccountType;
import com.domeni.kapita.payment.domain.provider_transaction.*;
import com.domeni.kapita.payment.domain.transaction.*;
import com.domeni.kapita.payment.repositories.AccountRepository;
import com.domeni.kapita.payment.repositories.ProviderTransactionRepository;
import com.domeni.kapita.payment.repositories.TransactionRepository;
import com.domeni.kapita.payment.service.gateway.MonetbilGateway;
import com.domeni.kapita.payment.service.model.CreateTransferRequest;
import com.domeni.kapita.payment.service.model.CreatedTransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"all", "NullAway.Init"})
public class DepositProcessor {
    private final TransactionRepository transactionRepository;
    private final ProviderTransactionRepository providerTransactionRepository;
    private final AccountRepository accountRepository;
    private final MonetbilGateway monetbilGateway;

    @Value("${app.monetbil.service-key}")
    private String serviceKey;

    @Value("${app.monetbil.webhook-base-url}")
    private String webhookBaseUrl;

    @Transactional
    public CreatedTransferRequest process(CreateTransferRequest request) {
        // Idempotency check
        return transactionRepository
                .findByIdempotencyKey(request.idempotencyKey())
                .map(
                        existing -> {
                            String paymentUrl = providerTransactionRepository
                                    .findByTransactionId(existing.getId())
                                    .map(ProviderTransaction::getPaymentUrl)
                                    .orElse(null);
                            return CreatedTransferRequest.of(
                                    existing.getId().toString(), paymentUrl, existing);
                        })
                .orElseGet(
                        () -> {
                            Account platformAccount =
                                    accountRepository.findAll().stream()
                                            .filter(a -> a.getType() == AccountType.INTERNAL)
                                            .findFirst()
                                            .orElseThrow();

                            Transaction transaction =
                                    Transaction.builder()
                                            .idempotencyKey(request.idempotencyKey())
                                            .status(TransactionStatus.PENDING)
                                            .type(TransactionType.DEPOSIT)
                                            .fromAccountId(platformAccount.getId())
                                            .toAccountId(platformAccount.getId())
                                            .amount(
                                                    javax.money.Monetary.getDefaultAmountFactory()
                                                            .setNumber(
                                                                    new java.math.BigDecimal(
                                                                            request.amount()))
                                                            .setCurrency(request.currency())
                                                            .create())
                                            .reason("Deposit via " + request.provider())
                                            .description(request.description())
                                            .build();
                            transactionRepository.save(transaction);

                            try {
                                WidgetPaymentRequestDto monetbilReq = new WidgetPaymentRequestDto();
                                monetbilReq.setAmount(new java.math.BigDecimal(request.amount()));
                                monetbilReq.setCurrency(request.currency());
                                monetbilReq.setPaymentRef(transaction.getId().toString());
                                monetbilReq.setPhone(request.phoneNumber());
                                monetbilReq.setNotifyUrl(
                                        webhookBaseUrl + "/" + transaction.getId().toString());

                                WidgetPaymentResponseDto monetbilRes =
                                        monetbilGateway.initiatePayment(serviceKey, monetbilReq);

                                ProviderTransaction providerTransaction =
                                        ProviderTransaction.builder()
                                                .transactionId(transaction.getId())
                                                .provider(request.provider())
                                                .paymentUrl(monetbilRes.getPaymentUrl())
                                                .status(ProviderTransactionStatus.PENDING)
                                                .build();
                                providerTransactionRepository.save(providerTransaction);

                                return CreatedTransferRequest.of(
                                        transaction.getId().toString(),
                                        monetbilRes.getPaymentUrl(),
                                        transaction);
                            } catch (Exception e) {
                                transaction.setStatus(TransactionStatus.FAILED);
                                transactionRepository.save(transaction);
                                throw new RuntimeException("Failed to initiate payment with provider", e);
                            }
                        });
    }
}
