package org.aut.steps.definition;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import net.thucydides.core.annotations.Steps;
import org.aut.common.Constants;
import org.junit.Assert;

public class HealthSteps extends BaseSteps{

    @Steps
    CommonSteps commonSteps;

    Response response;

    @When("^The client request for check health$")
    public void theClientRequestForCheckHealth() {

        System.out.println("Last Stored Access Token: " + Constants.getInstance().getLastStoredAccessToken());

        String url = getBaseUrl();
        String healthCheck = propertiesReaderHelper.getProperty("health-check");
        commonSteps.resetHeaders();
        commonSteps.addHeader("Authorization", "Bearer " + Constants.getInstance().getLastStoredAccessToken());
        commonSteps.addHeader("Content-Type", "application/json");

        this.response = commonSteps.getRequest(url, healthCheck);
    }

    @Then("^The check health service should be \"([^\"]*)\"$")
    public void theCheckHealthServiceShouldBe(String condition) throws Throwable {

        Assert.assertEquals("The status code should be 200, but was " + this.response.statusCode(), 200, this.response.statusCode());
        Assert.assertEquals("The type should be service, but was " + this.response.path("type"), "service", this.response.path("type"));
        Assert.assertEquals("The status should be " + condition + ", but was " + this.response.path("status"), condition, this.response.path("status"));

    }
}
