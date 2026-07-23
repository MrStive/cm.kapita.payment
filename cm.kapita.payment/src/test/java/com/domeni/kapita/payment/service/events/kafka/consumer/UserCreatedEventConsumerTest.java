package com.domeni.kapita.payment.service.events.kafka.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.domeni.kapita.kafka.inbound.KafkaInboundConsumer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class UserCreatedEventConsumerTest {

  @Mock private KafkaInboundConsumer kafkaInboundConsumer;
  @Mock private Acknowledgment acknowledgment;

  @InjectMocks private UserCreatedEventConsumer consumer;

  @Test
  void handleUserCreatedEventShouldDispatchAndAckTest() {
    byte[] raw = "{}".getBytes(StandardCharsets.UTF_8);

    consumer.handleUserCreatedEvent(raw, "authentis.user.created", 0, 10L, acknowledgment);

    then(kafkaInboundConsumer)
        .should()
        .consume(raw, "authentis.user.created", 0, 10L, acknowledgment);
  }

  @Test
  void handleUserCreatedEventShouldNotAckWhenDispatchFailsTest() {
    byte[] raw = "{}".getBytes(StandardCharsets.UTF_8);
    org.mockito.Mockito.doThrow(new RuntimeException("boom"))
        .when(kafkaInboundConsumer)
        .consume(raw, "authentis.user.created", 0, 10L, acknowledgment);

    assertThrows(
        RuntimeException.class,
        () ->
            consumer.handleUserCreatedEvent(raw, "authentis.user.created", 0, 10L, acknowledgment));

    verifyNoInteractions(acknowledgment);
  }
}
