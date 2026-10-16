package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import org.aut.helpers.RegistrationHelper;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NegativeSteps extends BaseSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(NegativeSteps.class);

    @Steps
    CommonSteps commonSteps;

    private Response response;

    public NegativeSteps() {
    }

    @When("^The client registers a new user without the API key$")
    public void clientRegistersWithoutApiKey() throws JsonProcessingException {
        RegistrationHelper registrationHelper = new RegistrationHelper();
        Map<String, String> registerMap = registrationHelper.getRegisterMap();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("username", registerMap.get("email"));
        payload.put("password", registerMap.get("password"));
        payload.put("confirm", registerMap.get("password"));

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        String baseUrl = getBaseUrl();
        String usersEndpoint = propertiesReaderHelper.getProperty("users");

        commonSteps.setHeadersWithoutApiKey();
        this.response = commonSteps.postRequest(baseUrl, usersEndpoint, body);
        LOGGER.info("Registration without API key responded with status {}", this.response.statusCode());
    }

    @When("^The client registers a new user with an invalid API key$")
    public void clientRegistersWithInvalidApiKey() throws JsonProcessingException {
        RegistrationHelper registrationHelper = new RegistrationHelper();
        Map<String, String> registerMap = registrationHelper.getRegisterMap();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("username", registerMap.get("email"));
        payload.put("password", registerMap.get("password"));
        payload.put("confirm", registerMap.get("password"));

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        String baseUrl = getBaseUrl();
        String usersEndpoint = propertiesReaderHelper.getProperty("users");

        commonSteps.setHeadersWithInvalidApiKey("invalid_api_key_test_value");
        this.response = commonSteps.postRequest(baseUrl, usersEndpoint, body);
        LOGGER.info("Registration with invalid API key responded with status {}", this.response.statusCode());
    }

    @When("^The client requests tasks without a Bearer token$")
    public void clientRequestsTasksWithoutBearerToken() {
        String baseUrl = getBaseUrl();
        String tasksEndpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersWithoutBearerToken();
        this.response = commonSteps.getRequest(baseUrl, tasksEndpoint);
        LOGGER.info("Tasks request without Bearer token responded with status {}", this.response.statusCode());
    }

    @When("^The client attempts registration with missing \"([^\"]*)\"$")
    public void clientAttemptsRegistrationWithMissingField(String missingField) throws JsonProcessingException {
        RegistrationHelper registrationHelper = new RegistrationHelper();
        Map<String, String> registerMap = registrationHelper.getRegisterMap();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();

        if (!"username".equalsIgnoreCase(missingField)) {
            payload.put("username", registerMap.get("email"));
        }
        if (!"password".equalsIgnoreCase(missingField)) {
            payload.put("password", registerMap.get("password"));
        }
        if (!"confirm".equalsIgnoreCase(missingField)) {
            payload.put("confirm", registerMap.get("password"));
        }

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        String baseUrl = getBaseUrl();
        String usersEndpoint = propertiesReaderHelper.getProperty("users");

        commonSteps.setHeadersBasic();
        this.response = commonSteps.postRequest(baseUrl, usersEndpoint, body);
        LOGGER.info("Registration missing {} responded with status {}", missingField, this.response.statusCode());
    }

    @When("^The client attempts to login with invalid credentials$")
    public void clientAttemptsLoginWithInvalidCredentials() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("username", "non_existent_user_99999@example.com");
        payload.put("password", "wrong_password_123");

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        String baseUrl = getBaseUrl();
        String authEndpoint = propertiesReaderHelper.getProperty("auth");

        commonSteps.setHeadersBasic();
        this.response = commonSteps.postRequest(baseUrl, authEndpoint, body);
        LOGGER.info("Login with invalid credentials responded with status {}", this.response.statusCode());
    }

    @When("^The user attempts to create a task with points (-?\\d+)$")
    public void userAttemptsToCreateTaskWithInvalidPoints(int points) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("name", "Negative Points Task");
        payload.put("priority", "low");
        payload.put("points", points);

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        String baseUrl = getBaseUrl();
        String tasksEndpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        this.response = commonSteps.postRequest(baseUrl, tasksEndpoint, body);
        LOGGER.info("Task creation with points {} responded with status {}", points, this.response.statusCode());
    }

    @When("^The user requests a task with id (\\d+)$")
    public void userRequestsTaskWithId(int taskId) {
        String baseUrl = getBaseUrl();
        String tasksEndpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        this.response = commonSteps.getRequest(baseUrl, tasksEndpoint + "/" + taskId);
        LOGGER.info("Get task {} responded with status {}", taskId, this.response.statusCode());
    }

    @When("^The user attempts to delete a task with id (\\d+)$")
    public void userAttemptsToDeleteTaskWithId(int taskId) {
        String baseUrl = getBaseUrl();
        String tasksEndpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        this.response = commonSteps.deleteRequest(baseUrl, tasksEndpoint + "/" + taskId);
        LOGGER.info("Delete task {} responded with status {}", taskId, this.response.statusCode());
    }

    @Then("^The response status code should be (\\d+)$")
    public void responseStatusCodeShouldBe(int expectedStatusCode) {
        Assertions.assertNotNull(this.response, "Expected a response, but response was null");
        Assertions.assertEquals(expectedStatusCode, this.response.statusCode(),
                String.format("Expected HTTP %d but got HTTP %d: %s",
                        expectedStatusCode, this.response.statusCode(), this.response.asString()));
    }
}
