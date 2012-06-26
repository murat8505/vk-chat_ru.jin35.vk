package com.jin35.vk.net.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.net.IVKRequest;
import com.jin35.vk.net.Token;

class VKRequestHTTPS implements IVKRequest {

    private static final long DEAFULT_TIMEOUT = 60000;

    @Override
    public JSONObject executeRequestToAPIServer(String methodName, Map<String, String> params) throws IOException, IllegalArgumentException {
        return executeRequestToAPIServer(methodName, params, DEAFULT_TIMEOUT);
    }

    @Override
    public JSONObject executeRequestToAPIServer(String methodName, Map<String, String> params, long timeout) throws IOException, IllegalArgumentException {
        String urlParams = "";
        if (params != null) {
            for (String paramKey : params.keySet()) {
                String paramValue = params.get(paramKey);
                if (paramValue != null) {
                    urlParams = urlParams.concat(paramKey).concat("=").concat(paramValue).concat("&");
                }
            }
        }
        urlParams = urlParams.concat("access_token=").concat(Token.getInstance().getToken());

        URI uri;
        try {
            uri = new URI("https", "api.vk.com", "/method/".concat(methodName), urlParams, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return executeRequest(uri.toASCIIString());

        // return executeRequest(responseUrl.concat(methodName).concat(urlParams));
    }

    @Override
    public JSONObject executeLoginRequest(String login, String pass) throws IOException, IllegalArgumentException {
        try {
            String fullUrl = "https://api.vk.com/oauth/token?grant_type=password&client_id=2967368&client_secret=5cb44w23rsUXv3TyNaFi&scope=notify,friends,photos,audio,video,messages,offline&username="
                    .concat(login).concat("&password=").concat(pass);
            System.out.println("send: " + fullUrl);

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(fullUrl);
            HttpResponse response = httpClient.execute(get);

            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                JSONObject jsonAnswer = new JSONObject(out.toString());
                return jsonAnswer;
            }
            if (statusLine.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new IllegalArgumentException("wrong login or passwrod");
            } else {
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        } catch (JSONException e) {
            throw new IOException("error in parsing json answer");
        }
    }

    @Override
    public JSONObject executeRequest(String fullUrl) throws IOException, IllegalArgumentException {
        return executeRequest(fullUrl, DEAFULT_TIMEOUT);
    }

    private JSONObject executeRequest(String fullUrl, long timeout) throws IOException, IllegalArgumentException {
        try {
            System.out.println("full url: " + fullUrl);
            URL url = new URL(fullUrl);
            URLConnection conn = url.openConnection();
            String answer = "";

            ((HttpURLConnection) conn).setRequestMethod("GET");
            ((HttpURLConnection) conn).setReadTimeout((int) timeout);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                answer = answer.concat(inputLine);
            }
            in.close();

            JSONObject jsonAnswer = new JSONObject(answer);

            return jsonAnswer;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (JSONException e) {
            throw new IOException("error in parsing json answer");
        }
    }
}
