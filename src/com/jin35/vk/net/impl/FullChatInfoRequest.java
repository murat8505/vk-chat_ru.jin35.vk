package com.jin35.vk.net.impl;

import java.util.HashMap;
import java.util.Map;

import com.jin35.vk.net.IDataRequest;

public class FullChatInfoRequest implements IDataRequest {

    private final long chatId;

    public FullChatInfoRequest(long chatId) {
        this.chatId = chatId;
    }

    @Override
    public void execute() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            String code = "var chat_id=" + chatId + ";" + //
                    "var chat_info=API.messages.getChat({\"chat_id\":chatId});" + //
                    "return chat_info;";
            params.put("code", code);
            VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);
        } catch (Exception e) {
        }
    }

}
