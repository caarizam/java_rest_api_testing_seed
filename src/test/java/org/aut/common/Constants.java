package org.aut.common;

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

    final String LAST_TOKEN = "last_token";
    private Map<String, String> storedTokens = new HashMap<String, String>();

    public String getStoredAccessToken(String client){
        return storedTokens.get(client);
    }

    public String getLastStoredAccessToken(){
        return storedTokens.get(LAST_TOKEN);
    }

    public void addAccessToken(String client, String token){
        storedTokens.put(client, token);
        storedTokens.put(LAST_TOKEN, token);
    }
}
