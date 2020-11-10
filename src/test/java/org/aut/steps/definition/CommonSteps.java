package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.Step;

import java.util.HashMap;
import java.util.Map;

public class CommonSteps {

    Map<String, Object> headersMap;
    SerenityRest serenityRest;

    public CommonSteps(){
        headersMap = new HashMap<String, Object>();
        serenityRest = new SerenityRest();
    }

    @Step
    public Response getRequest(String baseUrl, String endPoint){
        try{
            Response response = SerenityRest.
                    given().
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
            Response response = SerenityRest.
                    given().
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
            Response response = SerenityRest.
                    given().
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
            Response response = SerenityRest.
                    given().
                    with().
                    headers(this.headersMap).
                    delete(baseUrl + endPoint);
            return response;
        }catch (Exception ex){
            System.out.println("Error on deleteAccountUser: " + ex.getMessage() + "\n" + ex.getCause());
            return null;
        }

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
