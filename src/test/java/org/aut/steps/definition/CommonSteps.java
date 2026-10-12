package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.aut.common.Constants;
import org.aut.helpers.PropertiesReaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.serenitybdd.annotations.Step;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class CommonSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSteps.class);
    PropertiesReaderHelper propertiesReaderHelper;

    private static final String API_KEY_HEADER = "x-api-key";

    static {
        // Serenity masks these headers in the console and in the reports
        RestAssured.config = RestAssured.config().logConfig(
                LogConfig.logConfig().blacklistHeaders(Set.of(API_KEY_HEADER, "Authorization")));
    }

    private final String apiKey;

    Map<String, Object> headersMap;
    SerenityRest serenityRest;

    public CommonSteps(){
        headersMap = new HashMap<String, Object>();
        serenityRest = new SerenityRest();
        propertiesReaderHelper = new PropertiesReaderHelper();
        this.apiKey = this.propertiesReaderHelper.getProperty("api-key");
    }

    public void setHeadersCredentials() {

        headersMap = new HashMap<String, Object>();
        headersMap.put("Content-Type", "application/json");
        headersMap.put(API_KEY_HEADER, this.apiKey);
        headersMap.put("Authorization", Constants.getInstance().getLastStoredAccessTokenValue());

    }

    public void setHeadersBasic() {

        headersMap = new HashMap<String, Object>();
        headersMap.put("Content-Type", "application/json");
        headersMap.put(API_KEY_HEADER, this.apiKey);

    }

    @Step
    public Response getRequest(String baseUrl, String endPoint){
        try{
            Response response = request().
                    with().
                    headers(this.headersMap).
                    with().
                    get(baseUrl + endPoint);
            return response;
        }catch (Exception ex){
            LOGGER.error("Error on getRequest", ex);
            return null;
        }
    }

    @Step
    public Response postClientRegistration(String baseUrl, String endPoint, String body){

        try{
            Response response = request().
                    with().
                    headers(this.headersMap).
                    with().
                    body(body).
                    post(baseUrl + endPoint);
            return response;
        }catch (Exception ex){
            LOGGER.error("Error on postClientRegistration", ex);
            return null;
        }

    }

    @Step
    public Response postRequest(String baseUrl, String endpoint, String body){

        // the body carries the password, so only the endpoint is logged
        LOGGER.info("POST request {}", endpoint);

        try{
            Response response = request().
                    with().
                    headers(this.headersMap).
                    with().
                    body(body).
                    post(baseUrl + endpoint);
            LOGGER.debug("POST Response: {}", response.getBody().asPrettyString());
            return response;
        }catch (Exception ex){
            LOGGER.error("Error on postRequest", ex);
            return null;
        }

    }

    @Step
    public Response deleteRequest(String baseUrl, String endPoint){

        try{
            Response response = request().
                    with().
                    headers(this.headersMap).
                    delete(baseUrl + endPoint);
            return response;
        }catch (Exception ex){
            LOGGER.error("Error on deleteRequest", ex);
            return null;
        }

    }

    @Step
    public Response deleteAccountUser(String baseUrl, String endPoint){

        try{
            Response response = request().
                    with().
                    headers(this.headersMap).
                    delete(baseUrl + endPoint);
            return response;
        }catch (Exception ex){
            LOGGER.error("Error on deleteAccountUser", ex);
            return null;
        }

    }

    private RequestSpecification request(){
        RequestSpecification spec = SerenityRest.given();
        if(!apiKey.isEmpty()){
            spec = spec.header(API_KEY_HEADER, apiKey);
        }
        return spec;
    }

    public void resetHeaders(){
        this.headersMap.clear();
    }

    public void addHeader(String key, String value){
        this.headersMap.put(key, value);
    }

    public String getJsonFromClass(Object obj) {

        try {
            ObjectWriter objw = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = objw.writeValueAsString(obj);
            return json;
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize {} to JSON", obj.getClass().getSimpleName(), e);
            return "{}";
        }

    }

}
