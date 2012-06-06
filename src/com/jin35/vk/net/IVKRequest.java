package com.jin35.vk.net;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

public interface IVKRequest {

    JSONObject executeRequest(String methodName, Map<String, String> params) throws IOException, IllegalArgumentException;
}
