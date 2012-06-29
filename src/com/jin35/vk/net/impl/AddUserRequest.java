package com.jin35.vk.net.impl;

import java.util.HashMap;
import java.util.Map;

import com.jin35.vk.net.IDataRequest;

public class AddUserRequest implements IDataRequest {

    private final long uid;

    public AddUserRequest(long uid) {
        this.uid = uid;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", String.valueOf(uid));
        try {
            VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("friends.add", params);
        } catch (Exception e) {
        }
    }
}