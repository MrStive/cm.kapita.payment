package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.exception.ResourceNotFoundException;
import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.payment.PaymentIntentId;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttemptId;
import com.domeni.kapita.payment.repositories.PaymentIntentRepository;
import com.domeni.kapita.payment.repositories.ProviderAttemptRepository;
import com.domeni.kapita.payment.service.model.PaymentNotification;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("NullAway.Init")
public class PaymentNotificationService {
    private final PaymentIntentRepository paymentIntentRepository;
    private final ProviderAttemptRepository providerAttemptRepository;
    private final PaymentStatusPublisher paymentStatusPublisher;

    @Transactional
    public void handle(PaymentNotification notification) {
        ProviderAttempt providerAttempt =
                providerAttemptRepository
                        .findById(new ProviderAttemptId(notification.providerAttemptId()))
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ProviderAttempt",
                                                notification.providerAttemptId().toString()));

        PaymentIntent paymentIntent =
                paymentIntentRepository
                        .findById(
                                new PaymentIntentId(providerAttempt.getPaymentIntentId().toUUID()))
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "PaymentIntent",
                                                providerAttempt.getPaymentIntentId().getValue()));

        if (paymentIntent.isFinal() || providerAttempt.isFinal()) {
            return;
        }

        verifyMoney(notification, paymentIntent);

        String rawPayload = formatPayload(notification.rawPayload());
        if (!applyStatus(notification, paymentIntent, providerAttempt, rawPayload)) {
            return;
        }

        providerAttemptRepository.save(providerAttempt);
        paymentIntentRepository.save(paymentIntent);
        paymentStatusPublisher.publish(paymentIntent, providerAttempt);
    }

    private boolean applyStatus(
            PaymentNotification notification,
            PaymentIntent paymentIntent,
            ProviderAttempt providerAttempt,
            @Nullable String rawPayload) {
        String rawStatus = notification.status();
        if (rawStatus == null) {
            throw new IllegalArgumentException("Missing provider payment status");
        }
        String status = normalizeStatus(rawStatus);
        return switch (status) {
            case "success" -> {
                providerAttempt.markSucceeded(notification.externalId(), rawPayload);
                paymentIntent.markSucceeded();
                yield true;
            }
            case "failed" -> {
                String reason = failureReason(notification);
                providerAttempt.markFailed(reason, rawPayload);
                paymentIntent.markFailed(reason);
                yield true;
            }
            case "cancelled", "canceled" -> {
                String reason = failureReason(notification);
                providerAttempt.markCancelled(reason, rawPayload);
                paymentIntent.markCancelled(reason);
                yield true;
            }
            case "expired" -> {
                String reason = failureReason(notification);
                providerAttempt.markExpired(reason, rawPayload);
                paymentIntent.markExpired(reason);
                yield true;
            }
            case "pending", "processing" -> false;
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported provider payment status: " + status);
        };
    }

    private void verifyMoney(PaymentNotification notification, PaymentIntent paymentIntent) {
        BigDecimal expectedAmount =
                paymentIntent.getAmount().getNumber().numberValueExact(BigDecimal.class);
        if (notification.amount() == null || expectedAmount.compareTo(notification.amount()) != 0) {
            throw new IllegalArgumentException("Payment notification amount does not match intent");
        }
        String expectedCurrency = paymentIntent.getAmount().getCurrency().getCurrencyCode();
        if (!expectedCurrency.equalsIgnoreCase(notification.currency())) {
            throw new IllegalArgumentException(
                    "Payment notification currency does not match intent");
        }
    }

    private String failureReason(PaymentNotification notification) {
        if (notification.status() == null || notification.status().isBlank()) {
            return "Missing provider status";
        }
        return "Provider status: " + notification.status();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Missing provider payment status");
        }
        return status.trim().toLowerCase();
    }

    private @Nullable String formatPayload(@Nullable Map<String, String> rawPayload) {
        return rawPayload == null ? null : rawPayload.toString();
    }
}
