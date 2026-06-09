package com.domeni.kapita.payment.e2e;

import com.domeni.kapita.generated.payment.event.dto.DomainEventType;
import com.domeni.kapita.generated.payment.event.dto.EmailAddressDTO;
import com.domeni.kapita.generated.payment.event.dto.UserCreatedEventDTO;
import com.domeni.kapita.generated.payment.event.dto.UserCreatedEventEnvelopeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class EventSteps {

    @Autowired private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired private ObjectMapper objectMapper;

    @When("An event with following data is received")
    public void anEventWithFollowingDataIsReceived(DataTable dataTable) throws Exception {
        Map<String, String> map = dataTable.asMaps(String.class, String.class).getFirst();
        String channel = map.get("channel");
        String eventType = map.get("event_type");
        kafkaTemplate
                .send(channel, objectMapper.writeValueAsString(createEnvelope(eventType, map)))
                .get();
    }

    private UserCreatedEventEnvelopeDTO createEnvelope(String eventType, Map<String, String> map) {
        if (!DomainEventType.USER_CREATED.name().equals(eventType)) {
            throw new IllegalArgumentException("Unsupported event type for e2e test");
        }

        UUID id = Optional.ofNullable(map.get("id")).map(UUID::fromString).orElse(null);
        String name = Optional.ofNullable(map.get("name")).orElse(map.get("username"));
        String firstname = map.get("firstname");
        String lastname = map.get("lastname");
        String email = map.get("email");

        UserCreatedEventDTO payload =
                new UserCreatedEventDTO()
                        .id(id)
                        .username(name)
                        .firstname(firstname)
                        .lastname(lastname)
                        .enabled(true)
                        .email(email == null ? null : new EmailAddressDTO().email(email));

        UUID eventId =
                Optional.ofNullable(map.get("event_id"))
                        .map(UUID::fromString)
                        .orElse(UUID.randomUUID());

        return new UserCreatedEventEnvelopeDTO()
                .eventId(eventId)
                .eventType(DomainEventType.USER_CREATED)
                .occurredAt(LocalDateTime.now())
                .payload(payload);
    }
}
