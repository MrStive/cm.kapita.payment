package com.domeni.kapita.payment.service.events.kafka;

import com.domeni.kapita.kafka.outbox.service.OutboxService;
import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.service.events.model.MoneyDTO;
import com.domeni.kapita.payment.service.events.model.PaymentStatusEvent;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("NullAway.Init")
public class PaymentStatusEventProducer implements PaymentStatusPublisher {
  private static final String PAYMENT_STATUS_CHANGED = "PAYMENT_STATUS_CHANGED";

  private final OutboxService outboxService;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(PaymentIntent paymentIntent, ProviderAttempt providerAttempt) {
    PaymentStatusEvent event =
        new PaymentStatusEvent(
            paymentIntent.getId().getValue(),
            paymentIntent.getExternalReference(),
            paymentIntent.getPurpose().name(),
            paymentIntent.getUserId(),
            paymentIntent.getStatus().name(),
            new MoneyDTO(
                paymentIntent.getAmount().getNumber().numberValue(BigDecimal.class),
                paymentIntent.getAmount().getCurrency().getCurrencyCode()),
            paymentIntent.getProvider(),
            providerAttempt.getId().getValue(),
            providerAttempt.getProviderReference(),
            paymentIntent.getFailureReason(),
            Instant.now());

    saveToOutbox(paymentIntent.getId().getValue(), PAYMENT_STATUS_CHANGED, event);
  }

  private void saveToOutbox(String aggregateId, String eventType, Object payload) {
    try {
      String jsonPayload = objectMapper.writeValueAsString(payload);
      outboxService.enqueue(aggregateId, eventType, jsonPayload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event payload", e);
    }
  }
}
