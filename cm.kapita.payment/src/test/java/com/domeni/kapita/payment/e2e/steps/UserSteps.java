package com.domeni.kapita.payment.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class UserSteps {

    @Autowired private JdbcClient jdbcClient;

    @Given("I assume there is no user with the following data in the database")
    public void iAssumeThereIsNoUserWithTheFollowingDataInTheDatabase(DataTable dataTable) {
        Map<String, String> map = dataTable.asMaps(String.class, String.class).getFirst();
        String id = map.get("id");

        assertThat(
                        jdbcClient
                                .sql("SELECT EXISTS(SELECT 1 FROM t_user WHERE c_id = ?)")
                                .param(id)
                                .query(Boolean.class)
                                .single())
                .isFalse();
    }

    @Then("I should see that there is a user with the following data in the database")
    public void iShouldSeeThatThereIsAUserWithTheFollowingDataInTheDatabase(DataTable dataTable) {
        Map<String, String> map = dataTable.asMaps(String.class, String.class).getFirst();
        String id = map.get("id");

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(
                        () -> {
                            Map<String, Object> row =
                                    jdbcClient
                                            .sql(
                                                    """
                                                    SELECT c_id AS id,
                                                           c_name AS name,
                                                           c_firstname AS firstname,
                                                           c_lastname AS lastname,
                                                           c_email AS email
                                                    FROM t_user
                                                    WHERE c_id = ?
                                                    """)
                                            .param(id)
                                            .query()
                                            .singleRow();

                            assertThat(row.get("id")).isEqualTo(id);
                            assertThat(row.get("name")).isEqualTo(map.get("name"));
                            assertThat(row.get("firstname")).isEqualTo(map.get("firstname"));
                            assertThat(row.get("lastname")).isEqualTo(map.get("lastname"));
                            assertThat(row.get("email")).isEqualTo(map.get("email"));
                        });
    }

    @Then("I should see that there is exactly {int} user with id {string} in the database")
    public void iShouldSeeThatThereIsExactlyUserWithIdInTheDatabase(int expectedCount, String id) {
        await().atMost(1000, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(
                        () -> {
                            Integer count =
                                    jdbcClient
                                            .sql("SELECT COUNT(1) FROM t_user WHERE c_id = ?")
                                            .param(id)
                                            .query(Integer.class)
                                            .single();

                            assertThat(count).isEqualTo(expectedCount);
                        });
    }

    @Then("I should see that the inbox contains exactly {int} event with id {string}")
    public void iShouldSeeThatTheInboxContainsExactlyEventWithId(
            int expectedCount, String eventId) {
        await().atMost(1000, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(
                        () -> {
                            Integer count =
                                    jdbcClient
                                            .sql(
                                                    "SELECT COUNT(1) FROM t_inbox_event WHERE c_id = ?")
                                            .param(eventId)
                                            .query(Integer.class)
                                            .single();

                            assertThat(count).isEqualTo(expectedCount);
                        });
    }
}
