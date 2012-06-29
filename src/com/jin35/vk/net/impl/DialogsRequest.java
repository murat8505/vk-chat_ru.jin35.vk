package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Pair;

import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;

public class DialogsRequest implements IDataRequest {

    @Override
    public void execute() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            String code = "var uids=API.messages.getDialogs()@.uid;" + "var i=uids.length;" + "var result=[];" + "while(i>0){" + "i=i-1;"
                    + "result=result+API.messages.getHistory({\"uid\":uids[i]});" + "}" + "return result;";
            params.put("code", code);
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);

            parseMessagesFromResponse(response, null, false);
        } catch (Exception e) {
            e.printStackTrace();
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
                if (!(array.get(i) instanceof JSONObject)) {
                    continue;
                }
                JSONObject oneMessage = array.getJSONObject(i);
                System.out.println("one msg: " + oneMessage);
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
                if (msg == null) {
                    msg = new Message(id, uid, text, time, income);
                } else {
                    msg.setTime(time);
                }
                msg.setRead(read);

                try {
                    if (oneMessage.has("geo")) {
                        JSONObject geo = oneMessage.getJSONObject("geo");
                        String rawLoc = geo.getString("coordinates");
                        String[] loc = rawLoc.split(" ");
                        msg.setLocation(new Pair<Double, Double>(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])));
                    }
                    if (oneMessage.has("attachments")) {
                        msg.setAttachmentPack(new AttachmentPack(oneMessage.getJSONArray("attachments")));
                    }
                } catch (Throwable e) {
                }
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
                for (int i = uids.size() - 1; i >= 0; i--) {
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getMessagesWithUserRequest(uids.get(i))));
                }
            }
        }
    }
}
