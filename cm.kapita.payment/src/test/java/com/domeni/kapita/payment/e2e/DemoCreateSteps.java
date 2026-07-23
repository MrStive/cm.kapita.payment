package com.domeni.kapita.payment.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.domeni.kapita.generated.payment.dto.CreateDemoDTO;
import com.domeni.kapita.payment.e2e.context.CreateFlowContext;
import com.domeni.kapita.payment.e2e.support.JwtTokenFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DemoCreateSteps {

  @LocalServerPort private int localServerPort;

  @Autowired private JdbcClient jdbcClient;

  @Autowired private JwtTokenFactory jwtTokenFactory;

  @Autowired private CreateFlowContext createFlowContext;

  @Given("Assume that I am connected with a token containing create scopes")
  public void assumeThatIAmConnectedWithATokenContainingCreateScopes(DataTable dataTable) {
    List<String> scopes =
        dataTable.asMaps(String.class, String.class).stream()
            .map(row -> row.get("scope"))
            .filter(scope -> scope != null && !scope.isBlank())
            .toList();

    assertThat(scopes).as("At least one create scope must be provided").isNotEmpty();

    createFlowContext.setBearerToken(jwtTokenFactory.createToken(scopes.toArray(new String[0])));

    assertThat(createFlowContext.getBearerToken())
        .as("A bearer token must be available before any operation")
        .isNotBlank();
  }

  @When("I call create demo API with payload")
  public void iCallCreateDemoApiWithPayload(DataTable dataTable) {
    assertThat(createFlowContext.getBearerToken())
        .as("A bearer token must be available before any operation")
        .isNotBlank();

    Map<String, String> payloadRow = dataTable.asMaps(String.class, String.class).getFirst();
    String demoName = payloadRow.get("name");

    createFlowContext.setRequestedDemoName(demoName);
    createFlowContext.setLastResponse(
        given()
            .port(localServerPort)
            .auth()
            .oauth2(createFlowContext.getBearerToken())
            .contentType(ContentType.JSON)
            .body(new CreateDemoDTO().name(demoName))
            .when()
            .post("/demo"));
  }

  @Then("the create response status should be {int}")
  public void theCreateResponseStatusShouldBe(int expectedStatus) {
    assertThat(createFlowContext.getLastResponse()).isNotNull();
    assertThat(createFlowContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
  }

  @Then("a created demo id is returned")
  public void aCreatedDemoIdIsReturned() {
    String rawId = createFlowContext.getLastResponse().jsonPath().getString("newId");
    assertThat(rawId).isNotBlank();
    createFlowContext.setCreatedDemoId(UUID.fromString(rawId));
  }

  @Then("I should see the created demo in database")
  public void iShouldSeeTheCreatedDemoInDatabase(DataTable dataTable) {
    Map<String, String> expectedRow = dataTable.asMaps(String.class, String.class).getFirst();

    Map<String, Object> actualRow =
        jdbcClient
            .sql("select c_name as name from t_demo where c_id = ?")
            .param(createFlowContext.getCreatedDemoId().toString())
            .query()
            .singleRow();

    assertThat(actualRow.get("name")).isEqualTo(expectedRow.get("name"));
  }
}
