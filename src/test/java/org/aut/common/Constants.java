package org.aut.common;

import org.aut.dto.AccessToken;
import org.aut.helpers.RegistrationHelper;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    private static Constants instance;

    private Constants() {
    }

    public static Constants getInstance() {
        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }

    final String ACCESS_TOKEN = "access_token";
    final String REFRESH_TOKEN = "refresh_token";
    final String TYPE_TOKEN = "token_type";
    final String LAST_TOKEN = "last_token";

    public boolean singleRegister = false;
    public boolean shouldICreateAUser = true;
    public String md5Scenario = "";

    public boolean isSingleRegister() {
        return singleRegister;
    }

    public void setSingleRegister(boolean singleRegister) {
        this.singleRegister = singleRegister;
    }

    public String getMd5Scenario() {
        return md5Scenario;
    }

    public void setMd5Scenario(String md5Scenario) {
        this.md5Scenario = md5Scenario;
    }

    public boolean isShouldICreateAUser() {
        return shouldICreateAUser;
    }

    public void setShouldICreateAUser(boolean shouldICreateAUser) {
        this.shouldICreateAUser = shouldICreateAUser;
    }

    public String getACCESS_TOKEN() {
        return ACCESS_TOKEN;
    }

    public String getREFRESH_TOKEN() {
        return REFRESH_TOKEN;
    }

    public String getTYPE_TOKEN() {
        return TYPE_TOKEN;
    }

    private Map<String, AccessToken> storedTokens = new HashMap<String, AccessToken>();

    public AccessToken getStoredAccessToken(String client){
        return storedTokens.get(client);
    }

    public AccessToken getLastStoredAccessToken(){
        return storedTokens.get(LAST_TOKEN);
    }

    public String getLastStoredAccessTokenValue(){
        return storedTokens.get(LAST_TOKEN).getType() + " " + storedTokens.get(LAST_TOKEN).getAccessToken();
    }


    public void addAccessToken(String client, String token, String refresh, String type){
        AccessToken accessToken = new AccessToken(token, refresh, type);
        storedTokens.put(client, accessToken);
        storedTokens.put(LAST_TOKEN, accessToken);
    }

    public void clearAccessTokens() {
        this.storedTokens.clear();
    }

    public boolean isAuthenticated() {

        return !this.storedTokens.isEmpty();

    }

    public String getMd5Feature(String featureUriName) {
        RegistrationHelper registrationHelper = new RegistrationHelper();
        return registrationHelper.generateMd5(featureUriName);
    }
}
