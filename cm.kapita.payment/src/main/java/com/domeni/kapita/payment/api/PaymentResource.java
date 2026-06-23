package com.domeni.kapita.payment.api;

import com.domeni.kapita.generated.payment.api.PaymentApi;
import com.domeni.kapita.generated.payment.dto.InitiatePaymentDTO;
import com.domeni.kapita.generated.payment.dto.PaymentResponseDTO;
import com.domeni.kapita.payment.infrastructure.adapter.monetbil.MonetbilWebhookAdapter;
import com.domeni.kapita.payment.service.PaymentService;
import com.domeni.kapita.payment.service.mapper.PaymentMapper;
import com.domeni.kapita.payment.service.model.CreatePaymentRequest;
import com.domeni.kapita.payment.service.model.CreatedPayment;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentResource implements PaymentApi {
    private final PaymentService paymentService;
    private final MonetbilWebhookAdapter monetbilWebhookAdapter;
    private final PaymentMapper paymentMapper;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<PaymentResponseDTO> initiatePayment(InitiatePaymentDTO dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new IllegalStateException("JWT authentication is required");
        }
        String userId = jwt.getSubject();

        CreatePaymentRequest paymentRequest = paymentMapper.toRequest(dto, userId);

        CreatedPayment response = paymentService.initiatePayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMapper.toResponse(response));
    }

    @Override
    public ResponseEntity<String> handleMonetbilWebhook(
            UUID providerAttemptId,
            String status,
            String amount,
            String currency,
            String transactionId,
            String sign) {
        Map<String, String> payload =
                request.getParameterMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
        monetbilWebhookAdapter.handle(providerAttemptId, payload);
        return ResponseEntity.ok("received");
    }
}
