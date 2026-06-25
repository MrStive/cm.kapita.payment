package com.domeni.kapita.payment.service.mapper;

import com.domeni.kapita.generated.payment.dto.InitiatePaymentDTO;
import com.domeni.kapita.generated.payment.dto.MoneyDTO;
import com.domeni.kapita.generated.payment.dto.PaymentResponseDTO;
import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.payment.PaymentPurpose;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.service.model.CreatePaymentRequest;
import com.domeni.kapita.payment.service.model.CreatedPayment;
import com.domeni.kapita.payment.service.model.Money;
import com.domeni.kapita.payment.service.ports.PaymentInitiationRequest;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "provider", expression = "java(dto.getProvider().getValue())")
    @Mapping(target = "purpose", expression = "java(dto.getPurpose().getValue())")
    CreatePaymentRequest toRequest(InitiatePaymentDTO dto, String userId);

    @Mapping(
            target = "paymentId",
            expression = "java(java.util.UUID.fromString(response.paymentId()))")
    @Mapping(target = "externalReference", source = "externalReference")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentUrl", source = "paymentUrl")
    PaymentResponseDTO toResponse(CreatedPayment response);

    @Mapping(target = "paymentId", source = "paymentIntent.id.value")
    @Mapping(target = "externalReference", source = "paymentIntent.externalReference")
    @Mapping(target = "status", expression = "java(paymentIntent.getStatus().name())")
    @Mapping(target = "paymentUrl", source = "paymentUrl")
    CreatedPayment toCreatedPayment(PaymentIntent paymentIntent, @Nullable String paymentUrl);

    @Mapping(target = "paymentIntentId", source = "paymentIntent.id")
    @Mapping(target = "providerAttemptId", source = "providerAttempt.id")
    @Mapping(target = "amount", source = "paymentIntent.amount")
    @Mapping(target = "phoneNumber", source = "request.phoneNumber")
    @Mapping(target = "provider", source = "request.provider")
    @Mapping(target = "returnUrl", source = "request.returnUrl")
    @Mapping(target = "description", source = "paymentIntent.description")
    PaymentInitiationRequest toInitiationRequest(
            CreatePaymentRequest request,
            PaymentIntent paymentIntent,
            ProviderAttempt providerAttempt);

    default PaymentIntent toPaymentIntent(CreatePaymentRequest request, String requestHash) {
        return PaymentIntent.create(
                request.idempotencyKey(),
                requestHash,
                request.externalReference(),
                PaymentPurpose.valueOf(request.purpose()),
                request.userId(),
                toMonetaryAmount(request.money()),
                request.provider(),
                request.description());
    }

    default ProviderAttempt toProviderAttempt(PaymentIntent paymentIntent, String provider) {
        return ProviderAttempt.create(paymentIntent.getId(), provider);
    }

    default MonetaryAmount toMonetaryAmount(Money money) {
        return Monetary.getDefaultAmountFactory()
                .setNumber(money.amount())
                .setCurrency(money.currency())
                .create();
    }

    default Money map(MoneyDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Money(dto.getAmount(), dto.getCurrency());
    }
}
