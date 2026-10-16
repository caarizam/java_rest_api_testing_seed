package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import org.aut.common.Constants;
import org.aut.dto.ClientRegistrationDto;
import org.aut.helpers.RegistrationHelper;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginSteps extends BaseSteps{

    public LoginSteps(){
    }

    @Steps
    CommonSteps commonSteps;

    Response response;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginSteps.class);

    @Given("^The user is registered$")
    public void aUserIsRegistered() throws JsonProcessingException {

        if (Constants.getInstance().isShouldICreateAUser()) {

            Response loginResponse, registerResponse;

            RegistrationHelper registrationHelper = new RegistrationHelper();
            ObjectMapper loginMapper = new ObjectMapper();
            ObjectMapper registerMapper = new ObjectMapper();
            ObjectNode loginNodePayload = loginMapper.createObjectNode();
            ObjectNode registerNodePayload = registerMapper.createObjectNode();

            String baseUrl = getBaseUrl();
            String authenticationEndpoint = propertiesReaderHelper.getProperty("auth");
            String registrationEndpoint = propertiesReaderHelper.getProperty("users");

            Map<String, String> registerMap = registrationHelper.getRegisterMap();
            LOGGER.info("Registration user request {}", registerMap.get("email"));

            // Login Payload
            loginNodePayload.put("username", registerMap.get("email"));
            loginNodePayload.put("password", registerMap.get("password"));
            String loginPayload = loginMapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginNodePayload);

            // Registration Payload
            registerNodePayload.put("username", registerMap.get("email"));
            registerNodePayload.put("password", registerMap.get("password"));
            registerNodePayload.put("confirm", registerMap.get("password"));
            String registerPayload = registerMapper.writerWithDefaultPrettyPrinter().writeValueAsString(registerNodePayload);

            commonSteps.setHeadersBasic();

            registerResponse = commonSteps.postRequest(baseUrl, registrationEndpoint, registerPayload);
            Assertions.assertEquals(201, registerResponse.statusCode(), "The status code should be 201, but was " + registerResponse.statusCode());

            loginResponse = commonSteps.postRequest(baseUrl, authenticationEndpoint, loginPayload);

            Assertions.assertEquals(200, loginResponse.statusCode(), "The status code should be 200, but was " + loginResponse.statusCode());
            Assertions.assertNotNull(loginResponse.path("access_token"), "The access_token should not be empty");

            Constants.getInstance().addAccessToken(
                    registerMap.get("email"),
                    loginResponse.path(Constants.getInstance().getACCESS_TOKEN()).toString(),
                    loginResponse.path(Constants.getInstance().getREFRESH_TOKEN()).toString(),
                    loginResponse.path(Constants.getInstance().getTYPE_TOKEN()).toString()
            );

            boolean singleRegister = Boolean.parseBoolean(propertiesReaderHelper.getProperty("single_register"));
            if (singleRegister) {
                Constants.getInstance().setShouldICreateAUser(false);
            }
        }
    }

    @Given("^A client asks for registration$")
    public void aClientAsksForRegistration(DataTable table) {
        List<ClientRegistrationDto> clientRegistrationList = table.asMaps().stream()
                .map(row -> new ObjectMapper().convertValue(row, ClientRegistrationDto.class))
                .collect(Collectors.toList());
        String url = getBaseUrl();
        String addUser = this.propertiesReaderHelper.getProperty("add-user");
        String payload = commonSteps.getJsonFromClass(clientRegistrationList.get(0));
        commonSteps.resetHeaders();
        commonSteps.addHeader("Content-Type", "application/json");
        this.response = commonSteps.postClientRegistration(url, addUser, payload);
        Assertions.assertEquals(201, this.response.statusCode(), "The status code should be 201, but was " + this.response.statusCode());
        Assertions.assertEquals("ok", this.response.path("status"), "The status should be Ok, but was " + this.response.path("status"));
        Assertions.assertEquals("created", this.response.path("message"), "The message should be Created, but was " + this.response.path("message"));

    }

    @Then("^The client should be able to login with \"([^\"]*)\" and \"([^\"]*)\"$")
    public void theClientShouldBeAbleToLoginWithAnd(String email, String password) throws Throwable {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode loginPayload = mapper.createObjectNode();
        loginPayload.put("email", email);
        loginPayload.put("password", password);

        String bodyPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginPayload);

        String url = getBaseUrl();
        String auth = propertiesReaderHelper.getProperty("auth");

        this.response = commonSteps.postRequest(url, auth, bodyPayload);
        Assertions.assertEquals(200, this.response.statusCode(), "The status code should be 200, but was " + this.response.statusCode());
        Assertions.assertNotNull(this.response.path("access_token"), "The access_token should not be empty");
    }

    @And("^The client should be able to delete his account$")
    public void theClientShouldBeAbleToDeleteHisAccount() {
        String deleteAccount = propertiesReaderHelper.getProperty("delete-user");
        commonSteps.resetHeaders();
        commonSteps.addHeader("Authorization", "Bearer " + this.response.path("access_token").toString());
        commonSteps.addHeader("Content-Type", "application/json");
        this.response = commonSteps.deleteAccountUser(getBaseUrl(), deleteAccount);
        Assertions.assertEquals(200, this.response.statusCode(), "The status code should be 200, but was " + this.response.statusCode());
        Assertions.assertEquals("ok", this.response.path("status"), "The status should be Ok, but was " + this.response.path("status"));
        Assertions.assertEquals("the account has been deleted", this.response.path("message"), "The message should be The account has been deleted, but was " + this.response.path("message"));

    }

    @Given("^The client is logged in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void theClientIsLoggedInAnd(String email, String password) throws Throwable {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode loginPayload = mapper.createObjectNode();
        loginPayload.put("username", email);
        loginPayload.put("password", password);

        String bodyPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginPayload);

        String url = getBaseUrl();
        String auth = propertiesReaderHelper.getProperty("auth");

        commonSteps.resetHeaders();
        commonSteps.addHeader("Content-Type", "application/json");

        this.response = commonSteps.postRequest(url, auth, bodyPayload);

        Constants.getInstance().addAccessToken(
                email,
                this.response.path(Constants.getInstance().getACCESS_TOKEN()).toString(),
                this.response.path(Constants.getInstance().getREFRESH_TOKEN()).toString(),
                this.response.path(Constants.getInstance().getTYPE_TOKEN()).toString()
        );

        Assertions.assertEquals(200, this.response.statusCode(), "The status code should be 200, but was " + this.response.statusCode());
        Assertions.assertNotNull(this.response.path("access_token"), "The access_token should not be empty");

    }
}
