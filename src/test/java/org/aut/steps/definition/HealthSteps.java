package org.aut.steps.definition;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import org.aut.common.Constants;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthSteps extends BaseSteps{

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthSteps.class);

    @Steps
    CommonSteps commonSteps;

    Response response;

    @When("^The client request for check health$")
    public void theClientRequestForCheckHealth() {

        LOGGER.info("Access token stored: {}", Constants.getInstance().getLastStoredAccessToken() != null);

        String url = getBaseUrl();
        String healthCheck = propertiesReaderHelper.getProperty("health-check");
        commonSteps.resetHeaders();
        commonSteps.addHeader("Authorization", "Bearer " + Constants.getInstance().getLastStoredAccessToken());
        commonSteps.addHeader("Content-Type", "application/json");

        this.response = commonSteps.getRequest(url, healthCheck);
    }

    @Then("^The check health service should be \"([^\"]*)\"$")
    public void theCheckHealthServiceShouldBe(String condition) throws Throwable {

        Assertions.assertEquals(200, this.response.statusCode(), "The status code should be 200, but was " + this.response.statusCode());
        Assertions.assertEquals("service", this.response.path("type"), "The type should be service, but was " + this.response.path("type"));
        Assertions.assertEquals(condition, this.response.path("status"), "The status should be " + condition + ", but was " + this.response.path("status"));

    }
}
