package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.net.IDataRequest;

public class SearchRequest implements IDataRequest {

    private final String searchString;

    public SearchRequest(String searchString) {
        this.searchString = searchString;
    }

    @Override
    public void execute() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("q", searchString);
            params.put("fields", BaseUsersRequest.getFileds());
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("users.search", params);
            JSONArray response = answer.getJSONArray(responseParam);
            List<UserInfo> users = new ArrayList<UserInfo>();
            for (int i = 0; i < response.length(); i++) {
                if (response.get(i) instanceof JSONObject) {
                    users.add(BaseUsersRequest.getUser(response.getJSONObject(i)));
                }
            }
            MessageStorage.getInstance().setSearchResults(searchString, users);
        } catch (Exception e) {
        }
    }
}
