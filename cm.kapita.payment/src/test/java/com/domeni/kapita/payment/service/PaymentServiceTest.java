package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.payment.PaymentPurpose;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.service.mapper.PaymentMapperImpl;
import com.domeni.kapita.payment.service.model.CreatePaymentRequest;
import com.domeni.kapita.payment.service.model.CreatedPayment;
import com.domeni.kapita.payment.service.model.Money;
import com.domeni.kapita.payment.service.ports.PaymentGateway;
import com.domeni.kapita.payment.service.ports.PaymentGatewayResult;
import com.domeni.kapita.payment.service.ports.PaymentInitiationRequest;
import java.math.BigDecimal;
import java.util.Optional;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentPersistenceService paymentPersistenceService;
  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentRequestHasher paymentRequestHasher;

  private PaymentService paymentService;

  @BeforeEach
  void setUp() {
    paymentService =
        new PaymentService(
            paymentPersistenceService,
            paymentGateway,
            paymentRequestHasher,
            new PaymentMapperImpl());
  }

  @Test
  void shouldInitiatePaymentSuccessfully() {
    CreatePaymentRequest request =
        new CreatePaymentRequest(
            "id1",
            "subscription-order-1",
            "SUBSCRIPTION_PAYMENT",
            new Money(new BigDecimal("1000"), "XAF"),
            "699000000",
            "MONETBIL",
            "https://kapita.test/return",
            "user1",
            "desc");
    MonetaryAmount amount =
        Monetary.getDefaultAmountFactory()
            .setNumber(new BigDecimal("1000"))
            .setCurrency("XAF")
            .create();
    PaymentIntent intent =
        PaymentIntent.create(
            "id1",
            "hash",
            "subscription-order-1",
            PaymentPurpose.SUBSCRIPTION_PAYMENT,
            "user1",
            amount,
            "MONETBIL",
            "desc");
    ProviderAttempt attempt = ProviderAttempt.create(intent.getId(), "MONETBIL");
    when(paymentRequestHasher.hash(request)).thenReturn("hash");
    when(paymentPersistenceService.findByIdempotencyKey("id1")).thenReturn(Optional.empty());
    when(paymentPersistenceService.create(any(), any()))
        .thenReturn(new PaymentPersistenceService.LocalPayment(intent, attempt));
    when(paymentGateway.initiatePayment(any(PaymentInitiationRequest.class)))
        .thenReturn(new PaymentGatewayResult("provider-ref", "http://monetbil.com/pay", "{}"));
    when(paymentPersistenceService.markPaymentPending(any(), any(), any(), any(), any()))
        .thenAnswer(
            invocation -> {
              PaymentIntent savedIntent = invocation.getArgument(0);
              ProviderAttempt savedAttempt = invocation.getArgument(1);
              savedAttempt.markPending(
                  invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4));
              savedIntent.markPaymentPending();
              return new PaymentPersistenceService.LocalPayment(savedIntent, savedAttempt);
            });

    CreatedPayment result = paymentService.initiatePayment(request);

    verify(paymentPersistenceService).create(any(), any());
    verify(paymentGateway).initiatePayment(any(PaymentInitiationRequest.class));
    assertThat(result.externalReference()).isEqualTo("subscription-order-1");
    assertThat(result.status()).isEqualTo("PAYMENT_PENDING");
    assertThat(result.paymentUrl()).isEqualTo("http://monetbil.com/pay");
  }
}
