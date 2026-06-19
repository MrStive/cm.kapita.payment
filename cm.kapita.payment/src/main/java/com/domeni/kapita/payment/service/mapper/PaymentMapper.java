package com.domeni.kapita.payment.service.mapper;

import com.domeni.kapita.generated.payment.dto.InitiatePaymentDTO;
import com.domeni.kapita.generated.payment.dto.PaymentResponseDTO;
import com.domeni.kapita.payment.service.model.CreateTransferRequest;
import com.domeni.kapita.payment.service.model.CreatedTransferRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "returnUrl", constant = "")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "provider", expression = "java(dto.getProvider().getValue())")
    CreateTransferRequest toRequest(InitiatePaymentDTO dto);

    @Mapping(target = "transactionId", expression = "java(java.util.UUID.fromString(response.transactionId()))")
    @Mapping(target = "paymentUrl", source = "paymentUrl")
    PaymentResponseDTO toResponse(CreatedTransferRequest response);
}
