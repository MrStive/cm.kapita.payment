package com.domeni.kapita.payment.service.events.kafka;

import com.domeni.kapita.payment.service.events.model.PaymentStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"all", "NullAway.Init"})
public class PaymentStatusEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kapita.messaging.payment-status.topic}")
    private String topic;

    public void send(PaymentStatusEvent event) {
        kafkaTemplate.send(topic, event.transactionId(), event);
    }
}
