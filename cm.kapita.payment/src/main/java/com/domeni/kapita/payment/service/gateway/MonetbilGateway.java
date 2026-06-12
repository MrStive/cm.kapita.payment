package com.domeni.kapita.payment.service.gateway;

import com.domeni.kapita.generated.monetbil.api.MonetbilApi;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentRequestDto;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("NullAway")
public class MonetbilGateway {
    private final MonetbilApi monetbilApi;

    public WidgetPaymentResponseDto initiatePayment(
            String serviceKey, WidgetPaymentRequestDto request) {
        return monetbilApi.generatePaymentLink(serviceKey, request).getBody();
    }
}
