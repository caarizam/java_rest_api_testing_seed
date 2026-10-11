package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.aut.helpers.PropertiesReaderHelper;
import net.serenitybdd.annotations.Step;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class CommonSteps {

    private static final String API_KEY_HEADER = "x-api-key";

    static {
        // Serenity masks these headers in the console and in the reports
        RestAssured.config = RestAssured.config().logConfig(
                LogConfig.logConfig().blacklistHeaders(Set.of(API_KEY_HEADER, "Authorization")));
    }

    private final String apiKey = new PropertiesReaderHelper().getProperty("api-key");

    Map<String, Object> headersMap;
    SerenityRest serenityRest;

    public CommonSteps(){
        headersMap = new HashMap<String, Object>();
        serenityRest = new SerenityRest();
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
            System.out.println("Error on getRequest: " + ex.getMessage() + "\n" + ex.getCause());
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
            System.out.println("Error on postClientRegistration: " + ex.getMessage() + "\n" + ex.getCause());
            return null;
        }

    }

    @Step
    public Response postClientLogin(String baseUrl, String endPoint, String body){

        System.out.println(endPoint);
        System.out.println(body);

        try{
            Response response = request().
                    with().
                    headers(this.headersMap).
                    with().
                    body(body).
                    post(baseUrl + endPoint);
            System.out.println(response.getBody().prettyPrint());
            return response;
        }catch (Exception ex){
            System.out.println("Error on postClientLogin: " + ex.getMessage() + "\n" + ex.getCause());
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
            System.out.println("Error on deleteAccountUser: " + ex.getMessage() + "\n" + ex.getCause());
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
            e.printStackTrace();
            return "{}";
        }

    }

}
