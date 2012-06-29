package com.jin35.vk.net;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

public interface IVKRequest {

    JSONObject executeRequestToAPIServer(String methodName, Map<String, String> params) throws IOException, IllegalArgumentException;

    JSONObject executeRequestToAPIServer(String methodName, Map<String, String> params, long timeout) throws IOException, IllegalArgumentException;

    JSONObject executeRequest(String fullUrl) throws IOException, IllegalArgumentException;

    JSONObject executeLoginRequest(String login, String pass) throws IOException, IllegalArgumentException;

    JSONObject executePost(String serverUrl, String dataName, String dataType, byte[] dataValue) throws IOException, IllegalArgumentException;
}
