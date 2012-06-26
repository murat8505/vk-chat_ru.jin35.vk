package com.jin35.vk.net.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.net.IDataRequest;

public class DeleteMessagesRequest implements IDataRequest {

    private final String mids;

    DeleteMessagesRequest(String mids) {
        this.mids = mids;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("mids", mids);
        try {
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.delete", params);
            boolean success = response.has(responseParam);
            System.out.println("deleting messages [" + mids + "], " + success);
            List<Message> msgs = new ArrayList<Message>();
            for (String smid : mids.split(",")) {
                long mid = Long.parseLong(smid);
                Message msg = MessageStorage.getInstance().getMessageById(mid);
                if (msg != null) {
                    msgs.add(msg);
                    msg.setDeleting(false);
                }
            }
            if (success && msgs.size() > 0) {
                MessageStorage.getInstance().deleteMessage(msgs);
                MessageStorage.getInstance().dump();
            }

        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        }
    }

}
