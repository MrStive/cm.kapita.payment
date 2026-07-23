package com.domeni.kapita.payment.infrastructure.adapter.monetbil;

import com.domeni.kapita.payment.service.PaymentNotificationService;
import com.domeni.kapita.payment.service.model.PaymentNotification;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("NullAway.Init")
public class MonetbilWebhookAdapter {
  private final PaymentNotificationService paymentNotificationService;

  @Value("${app.monetbil.service-secret}")
  private String serviceSecret;

  public void handle(UUID providerAttemptId, Map<String, String> payload) {
    verifySignature(payload);

    PaymentNotification notification =
        new PaymentNotification(
            providerAttemptId,
            payload.get("status"),
            payload.get("transaction_id"),
            parseAmount(payload.get("amount")),
            payload.get("currency"),
            payload);

    paymentNotificationService.handle(notification);
  }

  private @Nullable BigDecimal parseAmount(@Nullable String rawAmount) {
    if (rawAmount == null || rawAmount.isBlank()) {
      return null;
    }
    return new BigDecimal(rawAmount);
  }

  private void verifySignature(Map<String, String> payload) {
    String receivedSign = payload.get("sign");
    if (receivedSign == null || receivedSign.isBlank()) {
      throw new IllegalArgumentException("Missing Monetbil sign");
    }

    if (serviceSecret == null || serviceSecret.isBlank()) {
      throw new IllegalStateException("Monetbil service secret is not configured");
    }

    List<Map.Entry<String, String>> entries = new ArrayList<>(payload.entrySet());
    entries.removeIf(entry -> "sign".equals(entry.getKey()));
    entries.sort(Map.Entry.comparingByKey());

    String joinedValues =
        entries.stream()
            .map(entry -> entry.getValue() == null ? "" : entry.getValue())
            .collect(Collectors.joining(","));
    String expectedSign = md5Hex(serviceSecret + joinedValues);

    if (!MessageDigest.isEqual(
        receivedSign.getBytes(StandardCharsets.UTF_8),
        expectedSign.getBytes(StandardCharsets.UTF_8))) {
      throw new IllegalArgumentException("Invalid Monetbil sign");
    }
  }

  private String md5Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        builder.append(String.format("%02x", b));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm not available", e);
    }
  }
}
