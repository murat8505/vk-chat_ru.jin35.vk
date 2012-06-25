package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;

public class RequestsRequest implements IDataRequest {

    @Override
    public void execute() {
        try {
            String executeRequest = "return API.getProfiles({\"uids\":API.friends.getRequests(),\"fields\":\"uid,first_name,last_name,online,photo_medium_rec\"});";

            Map<String, String> params = new HashMap<String, String>();
            params.put("code", executeRequest);
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);
            if (answer.has(responseParam)) {
                JSONArray array = answer.getJSONArray(responseParam);
                List<Long> ids = new ArrayList<Long>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject oneAnswer = array.getJSONObject(i);
                    long id = oneAnswer.getLong("uid");
                    UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(id, false);
                    user.setFamilyName(oneAnswer.getString("last_name"));
                    user.setName(oneAnswer.getString("first_name"));
                    user.setOnline(oneAnswer.getInt("online") == 1);
                    user.setPhotoUrl(oneAnswer.getString("photo_medium_rec"));
                    ids.add(id);
                }
                UserStorageFactory.getInstance().getUserStorage().markAsRequest(ids);
                UserStorageFactory.getInstance().getUserStorage().dump();
            } else {
                System.out.println("no response to exe");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}