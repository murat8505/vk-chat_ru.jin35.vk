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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.net.IAuthFailedHandler;
import com.jin35.vk.net.ICaptchaHandler;
import com.jin35.vk.net.IVKRequest;
import com.jin35.vk.net.Token;

class VKRequestHTTPS implements IVKRequest {

    private static final long DEAFULT_TIMEOUT = 60000;

    private final ICaptchaHandler capcthaHandler;
    private final IAuthFailedHandler authFailedHandler;

    public VKRequestHTTPS(ICaptchaHandler capcthaHandler, IAuthFailedHandler authFailedHandler) {
        this.capcthaHandler = capcthaHandler;
        this.authFailedHandler = authFailedHandler;
    }

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
            throw new IllegalArgumentException(e);
        }
        String url = uri.toASCIIString();
        if (url.contains("+")) {
            url = url.replace("+", "%2B");
        }
        return executeRequest(url);

        // return executeRequest(responseUrl.concat(methodName).concat(urlParams));
    }

    @Override
    public JSONObject executeLoginRequest(String login, String pass) throws IOException, IllegalArgumentException {
        return executeLoginRequest(login, pass, null, null);
    }

    private JSONObject executeLoginRequest(String login, String pass, String captcha_sid, String capthcha_key) throws IOException, IllegalArgumentException {
        try {
            String fullUrl = "https://api.vk.com/oauth/token?grant_type=password&client_id=" + Token.appId() + "&client_secret=" + Token.appSecret()
                    + "&scope=notify,friends,photos,audio,video,messages,offline&username=".concat(login).concat("&password=").concat(pass);
            if (captcha_sid != null && capthcha_key != null) {
                fullUrl += "&captcha_sid=" + captcha_sid + "&captcha_key=" + capthcha_key;
            }

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(fullUrl);
            HttpResponse response = httpClient.execute(get);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            JSONObject jsonAnswer = new JSONObject(out.toString());

            if (jsonAnswer.has("captcha_sid")) {
                String captchaImageUrl = jsonAnswer.getString("captcha_img");
                try {
                    return executeLoginRequest(login, pass, jsonAnswer.getString("captcha_sid"), capcthaHandler.onCapchaNeeded(captchaImageUrl));
                } catch (InterruptedException e) {
                    return new JSONObject();
                }
            }

            return jsonAnswer;
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

    private JSONObject executeRequest(final String fullUrl, final long timeout) throws IOException, IllegalArgumentException {
        try {
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

            jsonAnswer = errorHandler(jsonAnswer, fullUrl);
            return jsonAnswer;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (JSONException e) {
            throw new IOException("error in parsing json answer");
        }
    }

    private JSONObject errorHandler(JSONObject jsonAnswer, String fullUrl) throws JSONException, IllegalArgumentException, IOException {
        if (jsonAnswer.has("error")) {
            JSONObject error = jsonAnswer.getJSONObject("error");
            int code = error.getInt("error_code");
            switch (code) {
            case 14:
                String captchaImageUrl = error.getString("captcha_img");
                final String captcha_sid = error.getString("captcha_sid");
                try {
                    return executeRequest(fullUrl + "&captcha_sid=" + captcha_sid + "&captcha_key=" + capcthaHandler.onCapchaNeeded(captchaImageUrl));
                } catch (InterruptedException e) {
                    return new JSONObject();
                }
            case 6:
                try {
                    Thread.sleep(500);
                    return executeRequest(fullUrl);
                } catch (InterruptedException e) {
                    return new JSONObject();
                }
            case 4:
            case 5:
                authFailedHandler.onInvalidToken();
                break;
            case 2:
            case 7:
                authFailedHandler.onAccessDenied();
                break;
            default:
                break;
            }
        }
        return jsonAnswer;
    }

    @Override
    public JSONObject executePost(String serverUrl, String dataName, String dataType, byte[] dataValue) throws IOException, IllegalArgumentException {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(serverUrl);

            MultipartEntity me = new MultipartEntity();
            me.addPart(dataName, new ByteArrayBody(dataValue, dataType, "file.png"));

            post.setEntity(me);

            HttpResponse response = httpClient.execute(post);

            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                JSONObject jsonAnswer = new JSONObject(out.toString());
                return jsonAnswer;
            } else {
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (JSONException e) {
            throw new IOException("error in parsing json answer");
        }
    }
}
