package com.domeni.kapita.payment.service.gateway;

import com.domeni.kapita.generated.monetbil.api.MonetbilApi;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentRequestDto;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentRequestDto.CurrencyEnum;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto;
import com.domeni.kapita.payment.service.ports.PaymentGateway;
import com.domeni.kapita.payment.service.ports.PaymentGatewayResult;
import com.domeni.kapita.payment.service.ports.PaymentInitiationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("NullAway.Init")
public class MonetbilGateway implements PaymentGateway {
  private final MonetbilApi monetbilApi;
  private final ObjectMapper objectMapper;

  @Value("${app.monetbil.service-key}")
  private String serviceKey;

  @Value("${app.monetbil.webhook-base-url}")
  private String webhookBaseUrl;

  @Override
  public PaymentGatewayResult initiatePayment(PaymentInitiationRequest request) {
    WidgetPaymentRequestDto monetbilReq = new WidgetPaymentRequestDto();
    monetbilReq.setAmount(request.amount().getNumber().numberValueExact(BigDecimal.class));
    monetbilReq.setCurrency(CurrencyEnum.XAF);
    monetbilReq.setCountry(WidgetPaymentRequestDto.CountryEnum.CM);
    monetbilReq.setPaymentRef(request.paymentIntentId().getValue());
    monetbilReq.setPhone(request.phoneNumber());
    monetbilReq.setReturnUrl(request.returnUrl());
    monetbilReq.setNotifyUrl(webhookBaseUrl + "/" + request.providerAttemptId().getValue());

    WidgetPaymentResponseDto response =
        monetbilApi.generatePaymentLink(serviceKey, monetbilReq).getBody();

    if (response == null) {
      throw new IllegalStateException("Monetbil returned an empty response");
    }

    return new PaymentGatewayResult(
        response.getPaymentId(),
        Objects.requireNonNull(response.getPaymentUrl(), "paymentUrl is required"),
        serialize(response));
  }

  private String serialize(WidgetPaymentResponseDto response) {
    try {
      return objectMapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      return response.toString();
    }
  }
}
