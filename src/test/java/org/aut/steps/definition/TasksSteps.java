package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.aut.dto.Task;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.Objects;

public class TasksSteps extends BaseSteps {

    public TasksSteps() {}

    @Steps
    CommonSteps commonSteps;

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksSteps.class);

    List<Task> tasks;

    @When("^The user creates the task \"([^\"]*)\"$")
    public void aClientCreatesATask(String taskName) throws JsonProcessingException {

        Response response;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode taskPayload = mapper.createObjectNode();

        LOGGER.info("Task name for creation: {}", taskName);

        taskPayload.put("name", taskName);
        taskPayload.put("points", 5);
        taskPayload.put("priority", "low");

        String bodyPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskPayload);

        String baseUrl = getBaseUrl();
        String endpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        response = commonSteps.postRequest(baseUrl, endpoint, bodyPayload);
        Assertions.assertEquals(201, response.statusCode(), "The status code should be 201, but was " + response.statusCode());

    }

    @Then("^The user can see the created task \"([^\"]*)\"$")
    public void aClientCanSeeATask(String taskName) throws JsonProcessingException {

        Response response;

        String baseUrl = getBaseUrl();
        String endpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        response = commonSteps.getRequest(baseUrl, endpoint);
        Assertions.assertEquals(200, response.statusCode(), "The status code should be 200, but was " + response.statusCode());

        this.tasks = new ObjectMapper().readValue(response.asString(), new TypeReference<List<Task>>() {});
        LOGGER.info("Tasks received: {}", this.tasks.size());

        List<String> resultList = this.tasks.stream().map(Task::getName).toList();

        boolean isContained = resultList.contains(taskName);

        Assertions.assertTrue(isContained, String.format("The task %s was not found in the response", taskName));

    }

    @Then("^The user can delete the created task \"([^\"]*)\"$")
    public void aClientCanDeleteATask(String taskName){

        Response response;
        List<Integer> resultList = this.tasks.stream().filter(x -> Objects.equals(x.getName(), taskName)).map(Task::getId).toList();

        String baseUrl = getBaseUrl();
        String endpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        response = commonSteps.getRequest(baseUrl, endpoint);

        Assertions.assertAll("Verify task delete request",
                resultList.stream()
                        .map(p -> (Executable) () -> Assertions.assertTrue((commonSteps.deleteRequest(baseUrl, endpoint + "/" + p).statusCode() == 204))
                        )
                );

    }
}
