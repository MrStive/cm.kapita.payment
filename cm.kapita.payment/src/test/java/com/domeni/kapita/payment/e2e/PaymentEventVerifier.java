package com.domeni.kapita.payment.e2e;

import com.domeni.kapita.payment.service.events.model.PaymentStatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventVerifier {
    private final BlockingQueue<PaymentStatusEvent> events = new LinkedBlockingQueue<>();
    private final ObjectMapper objectMapper;

    public PaymentEventVerifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kapita.messaging.payment-status.topic}", groupId = "e2e-test")
    public void consume(ConsumerRecord<String, String> record) throws Exception {
        events.add(objectMapper.readValue(record.value(), PaymentStatusEvent.class));
    }

    public PaymentStatusEvent poll() {
        return events.poll();
    }
}
