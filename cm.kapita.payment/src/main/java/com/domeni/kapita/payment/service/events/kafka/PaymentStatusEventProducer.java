package com.domeni.kapita.payment.service.events.kafka;

import com.domeni.kapita.payment.domain.outbox.OutboxEvent;
import com.domeni.kapita.payment.domain.transaction.Transaction;
import com.domeni.kapita.payment.repositories.OutboxEventRepository;
import com.domeni.kapita.payment.service.events.model.MoneyDTO;
import com.domeni.kapita.payment.service.events.model.PaymentStatusEvent;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"all", "NullAway.Init"})
public class PaymentStatusEventProducer implements PaymentStatusPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(Transaction transaction) {
        PaymentStatusEvent event =
                new PaymentStatusEvent(
                        transaction.getId().getValue(),
                        transaction.getStatus().name(),
                        new MoneyDTO(
                                transaction.getAmount().getNumber().numberValue(BigDecimal.class),
                                transaction.getAmount().getCurrency().getCurrencyCode()));

        saveToOutbox(transaction.getId().getValue(), event);
    }

    private void saveToOutbox(String aggregateId, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxEvent outboxEvent = OutboxEvent.of(aggregateId, "PAYMENT_STATUS_CHANGED", jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    public void send(PaymentStatusEvent event) {
        saveToOutbox(event.transactionId(), event);
    }
}
