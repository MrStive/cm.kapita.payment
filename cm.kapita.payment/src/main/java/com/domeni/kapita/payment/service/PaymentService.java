package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.exception.IdempotencyConflictException;
import com.domeni.kapita.payment.domain.exception.PaymentProviderException;
import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.service.mapper.PaymentMapper;
import com.domeni.kapita.payment.service.model.CreatePaymentRequest;
import com.domeni.kapita.payment.service.model.CreatedPayment;
import com.domeni.kapita.payment.service.ports.PaymentGateway;
import com.domeni.kapita.payment.service.ports.PaymentGatewayResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"all", "NullAway.Init"})
public class PaymentService {
    private final PaymentPersistenceService paymentPersistenceService;
    private final PaymentGateway paymentGateway;
    private final PaymentRequestHasher paymentRequestHasher;
    private final PaymentMapper paymentMapper;

    public CreatedPayment initiatePayment(CreatePaymentRequest request) {
        validate(request);
        String requestHash = paymentRequestHasher.hash(request);

        return paymentPersistenceService
                .findByIdempotencyKey(request.idempotencyKey())
                .map(existing -> mapExistingPayment(existing, requestHash))
                .orElseGet(() -> createAndInitiatePayment(request, requestHash));
    }

    private CreatedPayment mapExistingPayment(PaymentIntent paymentIntent, String requestHash) {
        if (!paymentIntent.hasRequestHash(requestHash)) {
            throw new IdempotencyConflictException(paymentIntent.getIdempotencyKey());
        }

        String paymentUrl =
                paymentPersistenceService
                        .findAttempt(paymentIntent)
                        .map(ProviderAttempt::getPaymentUrl)
                        .orElse(null);

        return paymentMapper.toCreatedPayment(paymentIntent, paymentUrl);
    }

    private CreatedPayment createAndInitiatePayment(
            CreatePaymentRequest request, String requestHash) {
        PaymentIntent paymentIntent = paymentMapper.toPaymentIntent(request, requestHash);
        ProviderAttempt providerAttempt =
                paymentMapper.toProviderAttempt(paymentIntent, request.provider());
        PaymentPersistenceService.LocalPayment localPayment =
                paymentPersistenceService.create(paymentIntent, providerAttempt);
        paymentIntent = localPayment.paymentIntent();
        providerAttempt = localPayment.providerAttempt();

        PaymentGatewayResult gatewayResult;
        try {
            gatewayResult =
                    paymentGateway.initiatePayment(
                            paymentMapper.toInitiationRequest(
                                    request, paymentIntent, providerAttempt));
        } catch (Exception e) {
            paymentPersistenceService.markInitiationFailed(
                    paymentIntent, providerAttempt, "Failed to initiate provider payment");
            throw new PaymentProviderException(request.provider(), "Failed to initiate payment", e);
        }

        PaymentPersistenceService.LocalPayment pendingPayment =
                paymentPersistenceService.markPaymentPending(
                        paymentIntent,
                        providerAttempt,
                        gatewayResult.providerReference(),
                        gatewayResult.paymentUrl(),
                        gatewayResult.rawResponse());
        paymentIntent = pendingPayment.paymentIntent();
        providerAttempt = pendingPayment.providerAttempt();

        return paymentMapper.toCreatedPayment(paymentIntent, providerAttempt.getPaymentUrl());
    }

    private void validate(CreatePaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        requireText(request.idempotencyKey(), "idempotencyKey");
        requireText(request.externalReference(), "externalReference");
        requireText(request.purpose(), "purpose");
        requireText(request.userId(), "userId");
        requireText(request.phoneNumber(), "phoneNumber");
        requireText(request.provider(), "provider");
        if (request.money() == null) {
            throw new IllegalArgumentException("money is required");
        }
        if (request.money().amount() == null || request.money().amount().signum() <= 0) {
            throw new IllegalArgumentException("money.amount must be strictly positive");
        }
        requireText(request.money().currency(), "money.currency");
    }

    private void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
