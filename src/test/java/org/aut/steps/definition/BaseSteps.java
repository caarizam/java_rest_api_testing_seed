package org.aut.steps.definition;

import org.aut.helpers.PropertiesReaderHelper;

public class BaseSteps {

    private String urlBase = "url-base";
    private String portBase = "port";
    PropertiesReaderHelper propertiesReaderHelper;

    public BaseSteps(){
        propertiesReaderHelper = new PropertiesReaderHelper();
    }

    public String getBaseUrl(){
        String url = this.propertiesReaderHelper.getProperty("url-base");
        String port = this.propertiesReaderHelper.getProperty("port");

        return url + ":" + port;
    }
}
