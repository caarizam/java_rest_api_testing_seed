package org.aut.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReaderHelper {

    Properties properties;
    InputStream input;
    String fileName;

    public PropertiesReaderHelper(){
        properties = new Properties();
        input = null;
        this.fileName = "test-application.properties";
    }

    public String getProperty(String propertyName){

        String value = "";

        try{
            input = PropertiesReaderHelper.class.getClassLoader().getResourceAsStream(this.fileName);
            properties.load(input);
            value = properties.getProperty(propertyName);

            return value;

        }catch(IOException iex){
            iex.printStackTrace();

        }finally {
            if(input != null){
                try{
                    input.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        return "";
    }

}
