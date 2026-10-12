package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

public class TasksSteps extends BaseSteps {

    public TasksSteps() {}

    @Steps
    CommonSteps commonSteps;

    @When("^The user creates the task \"([^\"]*)\"$")
    public void aClientCreatesATask(String taskName) throws JsonProcessingException {

        Response response;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode taskPayload = mapper.createObjectNode();

        taskPayload.put("name", taskName);
        taskPayload.put("points", 5);
        taskPayload.put("priority", "low");

        String bodyPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskPayload);

        String baseUrl = getBaseUrl();
        String endpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        response = commonSteps.postRequest(baseUrl, endpoint, bodyPayload);
        Assertions.assertEquals(201, response.statusCode(), "The status code should be 200, but was " + response.statusCode());

    }

    @Then("^The user can see the created task \"([^\"]*)\"$")
    public void aClientCanSeeATask(String taskName){

    }

    @Then("^The user can delete the created task \"([^\"]*)\"$")
    public void aClientCanDeleteATask(String taskName){

    }
}
