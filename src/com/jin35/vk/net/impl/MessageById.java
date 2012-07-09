package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.db.DB;

public class MessageById extends BaseMessageRequest {

    private final long mid;

    public MessageById(long mid) {
        this.mid = mid;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("mid", String.valueOf(mid));

        try {
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.getById", params);

            if (response.has(responseParam)) {
                JSONArray messages = response.getJSONArray(responseParam);
                List<Message> msgs = new ArrayList<Message>();
                for (int i = 0; i < messages.length(); i++) {
                    if (messages.get(i) instanceof JSONObject) {
                        msgs.add(parseOneMessage(messages.getJSONObject(i), 0L, false));
                    }
                }
                requestAdditionalInfo();
                MessageStorage.getInstance().addMessages(msgs);
                for (Message message : msgs) {
                    DB.getInstance().saveMessage(message);
                }
            }
        } catch (Exception e) {
        }
    }

}
