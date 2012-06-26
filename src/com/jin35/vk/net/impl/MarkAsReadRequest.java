package com.jin35.vk.net.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jin35.vk.net.IDataRequest;

public class MarkAsReadRequest implements IDataRequest {

    private final String unreadMids;

    MarkAsReadRequest(String ureadMids) {
        this.unreadMids = ureadMids;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("mids", unreadMids);
        try {
            VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.markAsRead", params);
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        }
    }
}
