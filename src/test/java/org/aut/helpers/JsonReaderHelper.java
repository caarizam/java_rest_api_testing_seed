package org.aut.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class JsonReaderHelper {

    /*public LocalTvRatesFilters readFilters(String jsonText){

        Gson gson = new Gson();
        LocalTvRatesFilters filters = gson.fromJson(jsonText, LocalTvRatesFilters.class);

        return filters;

    }*/

    public String readJsonFile(String fileName){

        String tmpText, jsonText = "";

        try{

            File file = new File(JsonReaderHelper.class.getClassLoader().getResource(fileName).getPath());

            BufferedReader buffer = new BufferedReader(new FileReader(file));

            while((tmpText = buffer.readLine()) != null){
                jsonText += tmpText;
            }

            return jsonText;

        }catch (Exception ex){
            return null;
        }

    }
}
