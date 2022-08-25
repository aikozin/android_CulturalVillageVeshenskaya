package com.sidorov.culturalvillageveshenskaya;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {

    private final String JsonUrl;
    
    public API(String url) {
    	this.JsonUrl = url;
    }

    public JSONObject getJsonString() {
        try {
            URL url = new URL(JsonUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            if (reader != null)
            	reader.close();
            if (is != null)
            	is.close();
            return new JSONObject(json.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

