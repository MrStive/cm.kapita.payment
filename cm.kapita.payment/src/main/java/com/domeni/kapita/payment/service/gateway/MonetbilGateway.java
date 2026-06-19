package com.domeni.kapita.payment.service.gateway;

import com.domeni.kapita.generated.monetbil.api.MonetbilApi;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentRequestDto;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto;
import com.domeni.kapita.payment.domain.transaction.Transaction;
import com.domeni.kapita.payment.service.ports.PaymentGateway;
import com.domeni.kapita.payment.service.ports.PaymentInitiationRequest;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("NullAway")
public class MonetbilGateway implements PaymentGateway {
    private final MonetbilApi monetbilApi;

    @Value("${app.monetbil.service-key}")
    private String serviceKey;

    @Value("${app.monetbil.webhook-base-url}")
    private String webhookBaseUrl;

    @Override
    public Optional<String> initiatePayment(PaymentInitiationRequest request) {
        WidgetPaymentRequestDto monetbilReq = new WidgetPaymentRequestDto();
        monetbilReq.setAmount(
                request.amount().getNumber().numberValueExact(BigDecimal.class));
        monetbilReq.setCurrency(request.amount().getCurrency().getCurrencyCode());
        monetbilReq.setPaymentRef(request.transactionId().toString());
        monetbilReq.setPhone(request.phoneNumber());
        monetbilReq.setNotifyUrl(webhookBaseUrl + "/" + request.providerTransactionId().getValue());

        WidgetPaymentResponseDto response =
                monetbilApi.generatePaymentLink(serviceKey, monetbilReq).getBody();

        return Optional.ofNullable(response).map(WidgetPaymentResponseDto::getPaymentUrl);
    }
}
