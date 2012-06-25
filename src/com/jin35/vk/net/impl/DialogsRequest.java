package com.jin35.vk.net.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;

public class DialogsRequest implements IDataRequest {

    @Override
    public void execute() {
        try {
            // TODO replace with execute?
            parseMessagesFromResponse(VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.getDialogs", null), null, true);
        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        } catch (JSONException e) {
        }
    }

    static void parseMessagesFromResponse(JSONObject response, Long correspondentId, boolean requestMoreMessages) throws JSONException {
        if (response.has(responseParam)) {
            JSONArray array = response.getJSONArray(responseParam);
            // 0й элемент - количество записей
            List<Message> msgs = new ArrayList<Message>();
            List<Long> uidsWithoutInfo = new ArrayList<Long>();
            List<Long> uids = new ArrayList<Long>();
            for (int i = 1; i < array.length(); i++) {
                JSONObject oneMessage = array.getJSONObject(i);
                long id = oneMessage.getLong("mid");
                long uid;
                if (correspondentId == null) {
                    uid = oneMessage.getLong("uid");
                } else {
                    uid = correspondentId;
                }

                boolean read = oneMessage.getInt("read_state") == 1;
                Date time = new Date(oneMessage.getLong("date") * 1000);
                String text = oneMessage.getString("body");
                boolean income = oneMessage.getInt("out") == 0;
                Message msg = MessageStorage.getInstance().getMessageById(id);
                boolean newMsg = false;

                if (msg == null) {
                    msg = new Message(id, uid, text, time, income);
                    newMsg = true;
                } else {
                    msg.setTime(time);
                }
                msg.setRead(read);
                msg.notifyChanges();
                msgs.add(msg);

                if (UserStorageFactory.getInstance().getUserStorage().getUser(uid, true) == null) {
                    uidsWithoutInfo.add(uid);
                }
                if (!uids.contains(uid)) {
                    uids.add(uid);
                }
            }
            if (!uidsWithoutInfo.isEmpty()) {
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getUsersRequest(uidsWithoutInfo)));
            }
            MessageStorage.getInstance().addMessages(msgs);
            MessageStorage.getInstance().dump();
            if (requestMoreMessages) {
                for (Long id : uids) {
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getMessagesWithUserRequest(id)));
                }
            }
        }
    }
}
