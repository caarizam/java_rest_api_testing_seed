package org.aut.common;

import org.aut.dto.AccessToken;

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

    public boolean isAuthenticated() {

        return !this.storedTokens.isEmpty();

    }
}
