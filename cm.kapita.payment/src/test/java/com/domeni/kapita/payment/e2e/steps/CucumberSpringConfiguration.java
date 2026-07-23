package com.domeni.kapita.payment.e2e.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers
public class CucumberSpringConfiguration {

  @MockitoBean private com.domeni.kapita.generated.monetbil.api.MonetbilApi monetbilApi;

  @Container
  static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("payment")
          .withUsername("payment")
          .withPassword("payment")
          .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());

  @Container
  static final KafkaContainer KAFKA_CONTAINER =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));

  @DynamicPropertySource
  static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
    if (!POSTGRESQL_CONTAINER.isRunning()) {
      POSTGRESQL_CONTAINER.start();
    }
    if (!KAFKA_CONTAINER.isRunning()) {
      KAFKA_CONTAINER.start();
    }
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
    registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    registry.add("spring.jpa.properties.eclipselink.weaving", () -> "false");
    registry.add("spring.jpa.properties.eclipselink.ddl-generation", () -> "none");
    registry.add("spring.jpa.properties.eclipselink.target-database", () -> "PostgreSQL");
    registry.add("spring.liquibase.enabled", () -> "true");
    registry.add(
        "spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
    registry.add("spring.liquibase.contexts", () -> "e2e");
    registry.add("kapita.security.jwt.issuer", () -> "http://auth-service.local");
    registry.add("kapita.security.jwt.audience", () -> "kapita-api");
  }
}
