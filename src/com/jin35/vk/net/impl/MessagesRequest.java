package com.jin35.vk.net.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserStorage;
import com.jin35.vk.net.IDataRequest;

public class MessagesRequest implements IDataRequest {

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();

        try {
            System.out.println("send msg request");
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequest("messages.getDialogs", params);
            System.out.println("answer to msg request");
            if (answer.has("response")) {
                JSONArray array = answer.getJSONArray("response");
                // 0й элемент - количество записей
                System.out.println("response, " + array.get(0));
                List<Message> msgs = new ArrayList<Message>();
                List<Long> uidsWithoutInfo = new ArrayList<Long>();
                for (int i = 1; i < array.length(); i++) {
                    JSONObject oneMessage = array.getJSONObject(i);
                    long id = oneMessage.getLong("mid");
                    long correspondentId = oneMessage.getLong("uid");
                    Date time = new Date(oneMessage.getLong("date"));
                    String text = oneMessage.getString("body");
                    Message msg = new Message(id, correspondentId, text, time);
                    msgs.add(msg);

                    if (UserStorage.getInstance().getUser(correspondentId) == null) {
                        System.out.println("unknown user: " + correspondentId);
                        uidsWithoutInfo.add(correspondentId);
                    }
                }
                if (!uidsWithoutInfo.isEmpty()) {
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getUsersRequest(uidsWithoutInfo)));
                } else {
                    System.out.println("empty unknown user list");
                }
                MessageStorage.getInstance().addMessages(msgs);
            }

        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        } catch (JSONException e) {
        }
    }

}
