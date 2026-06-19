package com.domeni.kapita.payment.service.events.outbox;

import com.domeni.kapita.payment.domain.outbox.OutboxEvent;
import com.domeni.kapita.payment.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"all", "NullAway.Init"})
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kapita.messaging.payment-status.topic}")
    private String topic;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
                event.markAsProcessed();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event {}", event.getId(), e);
                event.markAsFailed();
                outboxEventRepository.save(event);
            }
        }
    }
}
