package com.domeni.kapita.payment.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.domeni.kapita.generated.monetbil.api.MonetbilApi;
import com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto;
import com.domeni.kapita.payment.e2e.support.JwtTokenFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;

public class PaymentDepositSteps {

  @LocalServerPort private int port;

  @Autowired private JdbcClient jdbcClient;
  @Autowired private JwtTokenFactory jwtTokenFactory;
  @Autowired private MonetbilApi monetbilApi;

  @Value("${app.monetbil.service-key}")
  private String serviceKey;

  @Value("${app.monetbil.service-secret}")
  private String serviceSecret;

  private String paymentId;
  private String providerAttemptId;

  @Given("I initiate a payment with following data")
  public void initiatePayment(DataTable dataTable) {
    Map<String, String> data = dataTable.asMaps().getFirst();
    String userId = "44a31cb2-bb38-4734-b8d8-9be15c7fb7b5";

    // Mock Monetbil response
    WidgetPaymentResponseDto mockResponse = new WidgetPaymentResponseDto();
    mockResponse.setPaymentUrl("https://monetbil.com/pay/test");
    mockResponse.setPaymentId("monetbil-test-id");
    when(monetbilApi.generatePaymentLink(eq(serviceKey), any()))
        .thenReturn(ResponseEntity.ok(mockResponse));

    String token = jwtTokenFactory.createToken("payment:initiate");

    var response =
        RestAssured.given()
            .port(port)
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(paymentPayload(data))
            .post("/payment");

    assertThat(response.statusCode()).isEqualTo(201);
    this.paymentId = response.jsonPath().getString("paymentId");

    // Extract providerAttemptId from DB
    this.providerAttemptId =
        jdbcClient
            .sql("SELECT c_id FROM t_provider_attempt WHERE c_payment_intent_id = ?")
            .param(paymentId)
            .query(String.class)
            .single();
  }

  @When("I receive a success notification from Monetbil")
  public void receiveSuccessNotification() {
    Map<String, String> payload = new TreeMap<>();
    payload.put("amount", "1000");
    payload.put("currency", "XAF");
    payload.put("status", "success");
    payload.put("transaction_id", "monetbil-tx-123");

    String joinedValues = payload.values().stream().collect(Collectors.joining(","));
    String sign = md5Hex(serviceSecret + joinedValues);

    payload.put("sign", sign);

    RestAssured.given()
        .port(port)
        .contentType(ContentType.URLENC)
        .formParams(payload)
        .post("/monetbil/webhook/" + providerAttemptId);
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Then("the transaction status should be {string}")
  public void verifyTransactionStatus(String status) {
    await()
        .atMost(5, java.util.concurrent.TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              String paymentStatus =
                  jdbcClient
                      .sql("SELECT c_status FROM t_payment_intent WHERE c_id = ?")
                      .param(paymentId)
                      .query(String.class)
                      .single();
              assertThat(paymentStatus).isEqualTo(status);
            });
  }

  @Then("a PaymentStatusEvent should be emitted to Kafka")
  public void verifyKafkaEvent() {
    // Implementation now handled via EventSteps.verifyPaymentStatusEvent("SUCCESS")
  }

  private Map<String, Object> paymentPayload(Map<String, String> data) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put(
        "externalReference", data.getOrDefault("externalReference", UUID.randomUUID().toString()));
    payload.put("purpose", data.getOrDefault("purpose", "SUBSCRIPTION_PAYMENT"));
    payload.put(
        "money",
        Map.of(
            "amount", data.get("amount"),
            "currency", data.get("currency")));
    payload.put("phoneNumber", data.get("phoneNumber"));
    payload.put("provider", data.get("provider"));
    payload.put("idempotencyKey", data.get("idempotencyKey"));
    payload.put("description", data.get("description"));
    payload.put("returnUrl", data.getOrDefault("returnUrl", "https://kapita.test/payment-return"));
    return payload;
  }
}
