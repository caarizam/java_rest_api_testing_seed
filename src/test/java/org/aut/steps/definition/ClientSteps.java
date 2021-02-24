package org.aut.steps.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.restassured.response.Response;
import net.thucydides.core.annotations.Steps;
import org.aut.common.Constants;
import org.aut.dto.ClientRegistrationDto;
import org.aut.helpers.PropertiesReaderHelper;
import org.junit.Assert;

import java.util.List;

public class ClientSteps extends BaseSteps{

    public ClientSteps(){
    }

    @Steps
    CommonSteps commonSteps;

    Response response;

    @Given("^A client asks for registration$")
    public void aClientAsksForRegistration(DataTable table) {
        List<ClientRegistrationDto> clientRegistrationList = table.asList(ClientRegistrationDto.class);
        String url = getBaseUrl();
        String addUser = this.propertiesReaderHelper.getProperty("add-user");
        String payload = commonSteps.getJsonFromClass(clientRegistrationList.get(0));
        commonSteps.resetHeaders();
        commonSteps.addHeader("Content-Type", "application/json");
        this.response = commonSteps.postClientRegistration(url, addUser, payload);
        Assert.assertEquals("The status code should be 201, but was " + this.response.statusCode(), 201, this.response.statusCode());
        Assert.assertEquals("The status should be Ok, but was " + this.response.path("status"), "ok", this.response.path("status"));
        Assert.assertEquals("The message should be Created, but was " + this.response.path("message"), "created", this.response.path("message"));

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

        this.response = commonSteps.postClientLogin(url, auth, bodyPayload);
        Assert.assertEquals("The status code should be 200, but was " + this.response.statusCode(), 200, this.response.statusCode());
        Assert.assertNotNull("The access_token should not be empty", this.response.path("access_token"));
    }

    @And("^The client should be able to delete his account$")
    public void theClientShouldBeAbleToDeleteHisAccount() {
        String deleteAccount = propertiesReaderHelper.getProperty("delete-user");
        commonSteps.resetHeaders();
        commonSteps.addHeader("Authorization", "Bearer " + this.response.path("access_token").toString());
        commonSteps.addHeader("Content-Type", "application/json");
        this.response = commonSteps.deleteAccountUser(getBaseUrl(), deleteAccount);
        Assert.assertEquals("The status code should be 200, but was " + this.response.statusCode(), 200, this.response.statusCode());
        Assert.assertEquals("The status should be Ok, but was " + this.response.path("status"), "ok", this.response.path("status"));
        Assert.assertEquals("The message should be The account has been deleted, but was " + this.response.path("message"), "the account has been deleted", this.response.path("message"));

    }

    @Given("^The client request for login with \"([^\"]*)\" and \"([^\"]*)\"$")
    public void theClientRequestForLoginWithAnd(String email, String password) throws Throwable {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode loginPayload = mapper.createObjectNode();
        loginPayload.put("email", email);
        loginPayload.put("password", password);

        String bodyPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginPayload);

        String url = getBaseUrl();
        String auth = propertiesReaderHelper.getProperty("auth");

        commonSteps.resetHeaders();
        commonSteps.addHeader("Content-Type", "application/json");

        this.response = commonSteps.postClientLogin(url, auth, bodyPayload);

        Constants.getInstance().addAccessToken(email, this.response.path("access_token").toString());

        Assert.assertEquals("The status code should be 200, but was " + this.response.statusCode(), 200, this.response.statusCode());
        Assert.assertNotNull("The access_token should not be empty", this.response.path("access_token"));

    }
}
