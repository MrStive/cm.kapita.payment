package com.domeni.kapita.payment.service.events.inbound;

import com.domeni.kapita.generated.payment.event.dto.DomainEventType;
import com.domeni.kapita.generated.payment.event.dto.UserCreatedEventDTO;
import com.domeni.kapita.kafka.inbound.InboundEventContext;
import com.domeni.kapita.kafka.inbound.InboundEventHandler;
import com.domeni.kapita.payment.service.UserService;
import com.domeni.kapita.payment.service.mapper.UserEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreatedInboundEventHandler implements InboundEventHandler<UserCreatedEventDTO> {

    private final UserService userService;
    private final UserEventMapper userEventMapper;

    @Override
    public void handle(UserCreatedEventDTO payload, InboundEventContext context) {
        userService.createUser(userEventMapper.map(payload));
    }

    @Override
    public Class<UserCreatedEventDTO> payloadType() {
        return UserCreatedEventDTO.class;
    }

    @Override
    public String eventType() {
        return DomainEventType.USER_CREATED.getValue();
    }
}
