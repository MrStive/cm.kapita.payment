package com.domeni.kapita.payment.api;

import com.domeni.kapita.generated.payment.api.PaymentApi;
import com.domeni.kapita.generated.payment.dto.InitiatePaymentDTO;
import com.domeni.kapita.generated.payment.dto.PaymentResponseDTO;
import com.domeni.kapita.payment.service.DepositProcessor;
import com.domeni.kapita.payment.service.MonetbilWebhookService;
import com.domeni.kapita.payment.service.model.CreateTransferRequest;
import com.domeni.kapita.payment.service.model.CreatedTransferRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentResource implements PaymentApi {
    private final DepositProcessor depositProcessor;
    private final MonetbilWebhookService monetbilWebhookService;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<PaymentResponseDTO> initiatePayment(InitiatePaymentDTO dto) {
        CreateTransferRequest request =
                new CreateTransferRequest(
                        UUID.randomUUID().toString(),
                        String.valueOf(dto.getAmount()),
                        dto.getCurrency(),
                        dto.getPhoneNumber(),
                        dto.getProvider().getValue(),
                        "",
                        dto.getDescription());

        CreatedTransferRequest response = depositProcessor.process(request);

        PaymentResponseDTO result = new PaymentResponseDTO();
        result.setTransactionId(UUID.fromString(response.transactionId()));
        result.setPaymentUrl(response.paymentUrl());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<String> handleMonetbilWebhook(UUID providerTransactionId) {
        Map<String, String> payload =
                request.getParameterMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
        monetbilWebhookService.handleNotification(providerTransactionId, payload);
        return ResponseEntity.ok("received");
    }
}
