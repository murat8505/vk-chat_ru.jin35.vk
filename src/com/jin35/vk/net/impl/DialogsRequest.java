package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    private final int limit;
    private final int offset;

    public DialogsRequest(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public void execute() {
        try {
            // TODO limit/offset
            Map<String, String> params = new HashMap<String, String>();
            String code = "var uids=API.messages.getDialogs({\"offset\":" + offset + ",\"count\":" + limit + "})@.uid;" + "var i=uids.length;"
                    + "var result=[];" + "while(i>0){" + "i=i-1;"
                    + "result=result+[{\"corrid\":uids[i], \"msgs\":API.messages.getHistory({\"uid\":uids[i]})}];" + "}" + "return result;";
            params.put("code", code);
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);

            parseMessagesFromResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void parseMessagesFromResponse(JSONObject response) throws JSONException {
        if (response.has(responseParam)) {
            JSONArray array = response.getJSONArray(responseParam);
            List<Message> msgs = new ArrayList<Message>();
            List<Long> uidsWithoutInfo = new ArrayList<Long>();
            int dialogsCount = 0;
            Map<Long, Integer> messageCounts = new HashMap<Long, Integer>();
            for (int i = 0; i < array.length(); i++) {
                if (!(array.get(i) instanceof JSONObject)) {
                    continue;
                }
                JSONObject dialog = array.getJSONObject(i);
                if (dialog.get("corrid") == JSONObject.NULL) {
                    continue;
                }
                Long correspondentId = dialog.getLong("corrid");
                JSONArray dialogMsgs = dialog.getJSONArray("msgs");
                dialogsCount++;
                int msgCount = 0;
                for (int j = 0; j < dialogMsgs.length(); j++) {
                    if (!(dialogMsgs.get(j) instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject oneMessage = dialogMsgs.getJSONObject(j);
                    Message msg = parseOneMessage(oneMessage, correspondentId);
                    msg.notifyChanges();
                    msgs.add(msg);
                    msgCount++;

                    if (UserStorageFactory.getInstance().getUserStorage().getUser(correspondentId, true) == null) {
                        uidsWithoutInfo.add(correspondentId);
                    }
                }
                messageCounts.put(correspondentId, msgCount);
            }
            if (!uidsWithoutInfo.isEmpty()) {
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getUsersRequest(uidsWithoutInfo)));
            }
            MessageStorage.getInstance().addMessages(msgs);
            MessageStorage.getInstance().setDownloadedDialogCount(messageCounts.size());
            for (Entry<Long, Integer> e : messageCounts.entrySet()) {
                MessageStorage.getInstance().setMessagesWithUserCount(e.getKey(), e.getValue());
            }
            MessageStorage.getInstance().dump();
        }
    }

    public static Message parseOneMessage(JSONObject message, long correspondentId) throws JSONException {
        System.out.println("one msg: " + message);
        long id = message.getLong("mid");
        boolean read = message.getInt("read_state") == 1;
        Date time = new Date(message.getLong("date") * 1000);
        String text = message.getString("body");
        boolean income = message.getInt("out") == 0;

        Message msg = MessageStorage.getInstance().getMessageById(id);
        if (msg == null) {
            msg = new Message(id, correspondentId, text, time, income);
        } else {
            msg.setTime(time);
        }
        msg.setRead(read);

        try {
            if (message.has("geo")) {
                JSONObject geo = message.getJSONObject("geo");
                String rawLoc = geo.getString("coordinates");
                String[] loc = rawLoc.split(" ");
                msg.setLocation(new Pair<Double, Double>(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])));
            }
            if (message.has("attachments")) {
                System.out.println("create attchments");
                msg.setAttachmentPack(new AttachmentPack(message.getJSONArray("attachments")));
                System.out.println("attacments count: " + msg.getAttachmentPack().size());
            }
        } catch (Throwable e) {
        }
        return msg;
    }
}
