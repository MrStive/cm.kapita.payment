package com.domeni.kapita.payment.e2e.context;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ScenarioScope
public class CreateFlowContext {
  private String bearerToken;
  private Response lastResponse;
  private UUID createdDemoId;
  private String requestedDemoName;
}
