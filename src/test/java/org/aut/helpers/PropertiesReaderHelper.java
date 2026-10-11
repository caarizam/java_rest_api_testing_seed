package org.aut.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Resolves a property in this order:
 * 1. environment variable (name upper-cased, "-" replaced by "_": api-key -> API_KEY)
 * 2. test-application.local.properties (git-ignored, optional)
 * 3. test-application.properties
 */
public class PropertiesReaderHelper {

    private static final String DEFAULT_FILE = "test-application.properties";
    private static final String LOCAL_FILE = "test-application.local.properties";

    private final Properties defaults = load(DEFAULT_FILE);
    private final Properties local = load(LOCAL_FILE);

    public String getProperty(String propertyName){

        String envValue = System.getenv(propertyName.toUpperCase(Locale.ROOT).replace('-', '_'));
        if(envValue != null && !envValue.isBlank()){
            return envValue;
        }

        String localValue = local.getProperty(propertyName);
        if(localValue != null && !localValue.isBlank()){
            return localValue;
        }

        return defaults.getProperty(propertyName, "");
    }

    private static Properties load(String fileName){
        Properties properties = new Properties();
        try(InputStream input = PropertiesReaderHelper.class.getClassLoader().getResourceAsStream(fileName)){
            if(input != null){
                properties.load(input);
            }
        }catch(IOException iex){
            iex.printStackTrace();
        }
        return properties;
    }

}
