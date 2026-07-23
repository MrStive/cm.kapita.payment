package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.payment.PaymentIntentStatus;
import com.domeni.kapita.payment.domain.payment.PaymentPurpose;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttemptStatus;
import com.domeni.kapita.payment.repositories.PaymentIntentRepository;
import com.domeni.kapita.payment.repositories.ProviderAttemptRepository;
import com.domeni.kapita.payment.service.model.PaymentNotification;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationServiceTest {

  @Mock private PaymentIntentRepository paymentIntentRepository;
  @Mock private ProviderAttemptRepository providerAttemptRepository;
  @Mock private PaymentStatusPublisher paymentStatusPublisher;

  @InjectMocks private PaymentNotificationService paymentNotificationService;

  @Test
  void shouldMarkPaymentAsCancelled() {
    PaymentFixture fixture = givenPendingPayment();

    paymentNotificationService.handle(notification(fixture, "cancelled"));

    assertThat(fixture.paymentIntent().getStatus()).isEqualTo(PaymentIntentStatus.CANCELLED);
    assertThat(fixture.providerAttempt().getStatus()).isEqualTo(ProviderAttemptStatus.CANCELLED);
    verify(providerAttemptRepository).save(fixture.providerAttempt());
    verify(paymentIntentRepository).save(fixture.paymentIntent());
    verify(paymentStatusPublisher).publish(fixture.paymentIntent(), fixture.providerAttempt());
  }

  @Test
  void shouldMarkPaymentAsExpired() {
    PaymentFixture fixture = givenPendingPayment();

    paymentNotificationService.handle(notification(fixture, "expired"));

    assertThat(fixture.paymentIntent().getStatus()).isEqualTo(PaymentIntentStatus.EXPIRED);
    assertThat(fixture.providerAttempt().getStatus()).isEqualTo(ProviderAttemptStatus.EXPIRED);
    verify(providerAttemptRepository).save(fixture.providerAttempt());
    verify(paymentIntentRepository).save(fixture.paymentIntent());
    verify(paymentStatusPublisher).publish(fixture.paymentIntent(), fixture.providerAttempt());
  }

  private PaymentFixture givenPendingPayment() {
    MonetaryAmount amount =
        Monetary.getDefaultAmountFactory()
            .setNumber(new BigDecimal("1000"))
            .setCurrency("XAF")
            .create();
    PaymentIntent paymentIntent =
        PaymentIntent.create(
            "idem-1",
            "hash",
            "subscription-order-1",
            PaymentPurpose.SUBSCRIPTION_PAYMENT,
            "user-1",
            amount,
            "MONETBIL",
            "Subscription payment");
    ProviderAttempt providerAttempt = ProviderAttempt.create(paymentIntent.getId(), "MONETBIL");
    paymentIntent.markPaymentPending();
    providerAttempt.markPending("provider-ref", "https://monetbil.test/pay", "{}");

    when(providerAttemptRepository.findById(providerAttempt.getId()))
        .thenReturn(Optional.of(providerAttempt));
    when(paymentIntentRepository.findById(paymentIntent.getId()))
        .thenReturn(Optional.of(paymentIntent));

    return new PaymentFixture(paymentIntent, providerAttempt);
  }

  private PaymentNotification notification(PaymentFixture fixture, String status) {
    return new PaymentNotification(
        fixture.providerAttempt().getId().toUUID(),
        status,
        "provider-ref",
        new BigDecimal("1000"),
        "XAF",
        Map.of("status", status));
  }

  private record PaymentFixture(PaymentIntent paymentIntent, ProviderAttempt providerAttempt) {}
}
