package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.service.model.CreatePaymentRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestHasher {

    public String hash(CreatePaymentRequest request) {
        String canonical =
                String.join(
                        "|",
                        nullToEmpty(request.externalReference()),
                        nullToEmpty(request.purpose()),
                        request.money() == null || request.money().amount() == null
                                ? ""
                                : request.money().amount().stripTrailingZeros().toPlainString(),
                        request.money() == null ? "" : nullToEmpty(request.money().currency()),
                        nullToEmpty(request.phoneNumber()),
                        nullToEmpty(request.provider()),
                        nullToEmpty(request.userId()));
        return sha256Hex(canonical);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
