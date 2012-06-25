package com.jin35.vk.net.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.jin35.vk.net.IDataRequest;

public class MessagesWithUserRequest implements IDataRequest {

    private final long uid;

    public MessagesWithUserRequest(long uid) {
        this.uid = uid;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", String.valueOf(uid));
        params.put("count", "20");

        try {
            DialogsRequest.parseMessagesFromResponse(VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.getHistory", params), uid,
                    false);
        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        } catch (JSONException e) {
        }
    }

}
