package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public class MessagesWithUserRequest extends BaseMessageRequest {

    private final long uid;
    private final int limit;
    private final int offset;

    public MessagesWithUserRequest(long uid, int limit, int offset) {
        this.uid = uid;
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", String.valueOf(uid));
        params.put("count", String.valueOf(limit));
        params.put("offset", String.valueOf(offset));

        try {
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.getHistory", params);

            if (response.has(responseParam)) {
                JSONArray messages = response.getJSONArray(responseParam);
                List<Message> msgs = new ArrayList<Message>();
                for (int i = 0; i < messages.length(); i++) {
                    if (messages.get(i) instanceof JSONObject) {
                        msgs.add(parseOneMessage(messages.getJSONObject(i), uid, false));
                    }
                }
                requestAdditionalInfo();
                MessageStorage.getInstance().addMessages(msgs);
                MessageStorage.getInstance().setMessagesWithUserCount(uid, msgs.size());
                MessageStorage.getInstance().dump();

                if (msgs.isEmpty()) {
                    NotificationCenter.getInstance().notifyConversationListeners(Arrays.asList(new Long[] { uid }));
                }
            }
        } catch (Exception e) {
        }
    }
}
