package com.jin35.vk.net.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.net.IVKRequest;
import com.jin35.vk.net.Token;

class VKRequestHTTPS implements IVKRequest {

    private static final String responseUrl = "https://api.vk.com/method/";

    @Override
    public JSONObject executeRequest(String methodName, Map<String, String> params) throws IOException, IllegalArgumentException {
        String urlParams = "?";
        if (params != null) {
            for (String paramKey : params.keySet()) {
                String paramValue = params.get(paramKey);
                if (paramValue != null) {
                    urlParams = urlParams.concat(paramKey).concat("=").concat(paramValue).concat("&");
                }
            }
        }
        urlParams = urlParams.concat("access_token=").concat(Token.getToken());

        URL url;
        try {
            url = new URL(responseUrl.concat(methodName).concat(urlParams));
            URLConnection conn = url.openConnection();
            String answer = "";

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                answer = answer.concat(inputLine);
            in.close();

            JSONObject jsonAnswer = new JSONObject(answer);

            return jsonAnswer;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        } catch (JSONException e) {
            throw new IOException("error in parsing json answer");
        }

    }
}
