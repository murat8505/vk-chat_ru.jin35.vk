package com.jin35.vk.net.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jin35.vk.model.Chat;
import com.jin35.vk.model.ChatStorage;
import com.jin35.vk.model.UserInfo;
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
            String code = "var chat_id=" + chatId + ";"
                    + //
                    "return {\"chat_info\":API.messages.getChat({\"chat_id\":chat_id}),\"chat_users\":API.messages.getChat({\"chat_id\":chat_id,\"fields\":\""
                    + BaseUsersRequest.getFileds() + "\"})};";
            params.put("code", code);
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);
            if (answer.has(responseParam)) {
                JSONObject response = answer.getJSONObject(responseParam);

                JSONObject chatInfo = response.getJSONObject("chat_info");
                Chat chat = new Chat(chatId);
                chat.setChatName(chatInfo.getString("title"));
                chat.setAdmin(chatInfo.getLong("admin_id"));

                JSONObject users = response.getJSONObject("chat_users");
                JSONArray array = users.getJSONArray("users");
                for (int i = 0; i < array.length(); i++) {
                    if (array.get(i) instanceof JSONObject) {
                        UserInfo user = BaseUsersRequest.getUser(array.getJSONObject(i));
                        chat.addUser(user.getId());
                    }
                }
                ChatStorage.getInstance().addChat(chat);
                chat.notifyChanges();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
