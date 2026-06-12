package com.domeni.kapita.payment.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;

public class PaymentDepositSteps {

    @LocalServerPort private int port;

    private String transactionId;
    private final JdbcClient jdbcClient;
    private final com.domeni.kapita.payment.e2e.PaymentEventVerifier paymentEventVerifier;

    public PaymentDepositSteps(
            JdbcClient jdbcClient,
            com.domeni.kapita.payment.e2e.PaymentEventVerifier paymentEventVerifier) {
        this.jdbcClient = jdbcClient;
        this.paymentEventVerifier = paymentEventVerifier;
    }

    @Given("I initiate a payment with following data")
    public void initiatePayment(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().getFirst();
        var response =
                RestAssured.given()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(data)
                        .post("/payment");

        assertThat(response.statusCode()).isEqualTo(201);
        this.transactionId = response.jsonPath().getString("transactionId");
    }

    @When("I receive a success notification from Monetbil")
    public void receiveSuccessNotification() {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.URLENC)
                .formParam("status", "success")
                .post("/monetbil/webhook/" + transactionId);
    }

    @Then("the transaction status should be COMPLETED")
    public void verifyTransactionStatus() {
        String status =
                jdbcClient
                        .sql("SELECT c_status FROM t_transaction WHERE c_id = ?")
                        .param(transactionId)
                        .query(String.class)
                        .single();
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Then("a PaymentStatusEvent should be emitted to Kafka")
    public void verifyKafkaEvent() {
        // Implementation now handled via EventSteps.verifyPaymentStatusEvent("SUCCESS")
    }
}
