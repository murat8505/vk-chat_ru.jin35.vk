package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Pair;

import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.ChatStorage;
import com.jin35.vk.model.ForwardedMsg;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;

public abstract class BaseMessageRequest implements IDataRequest {

    private final List<Long> uidsWithoutInfo = new ArrayList<Long>();
    private final List<Long> chatsWithoutInfo = new ArrayList<Long>();

    protected void requestAdditionalInfo() {
        if (!uidsWithoutInfo.isEmpty()) {
            BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getUsersRequest(uidsWithoutInfo)));
        }
        if (!chatsWithoutInfo.isEmpty()) {
            for (Long cid : chatsWithoutInfo) {
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getFullChatInfo(cid)));
            }
        }
    }

    private void markUidForRequest(Long uid) {
        if (!uidsWithoutInfo.contains(uid)) {
            uidsWithoutInfo.add(uid);
        }
    }

    private void markCidForRequest(Long cid) {
        if (!chatsWithoutInfo.contains(cid)) {
            chatsWithoutInfo.add(cid);
        }
    }

    protected Message parseOneMessage(JSONObject message, long correspondentId, boolean chat) throws JSONException {
        if (message.has("chat_id") || chat) {
            return parseOneChatMessage(message, correspondentId);
        }
        if (correspondentId == 0 && message.has("uid")) {
            correspondentId = message.getLong("uid");
        }
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

        if (UserStorageFactory.getInstance().getUserStorage().getUser(correspondentId, true) == null) {
            markUidForRequest(correspondentId);
        }

        parseAttaches(msg, message);

        return msg;
    }

    private ChatMessage parseOneChatMessage(JSONObject message, long chatId) throws JSONException {
        if (message.has("chat_id")) {
            chatId = message.getLong("chat_id");
        }
        long id = message.getLong("mid");
        boolean read = message.getInt("read_state") == 1;
        Date time = new Date(message.getLong("date") * 1000);
        String text = message.getString("body");
        long author = message.getLong("uid");
        boolean income = message.getInt("out") == 0;

        Message msg = MessageStorage.getInstance().getMessageById(id);
        ChatMessage result;
        if (msg == null || !(msg instanceof ChatMessage)) {
            result = new ChatMessage(id, chatId, text, time, income, author);
        } else {
            result = (ChatMessage) msg;
            result.setTime(time);
        }
        result.setRead(read);

        if (ChatStorage.getInstance().getChat(chatId) == null) {
            markCidForRequest(chatId);
        }

        if (UserStorageFactory.getInstance().getUserStorage().getUser(result.getAuthorId(), true) == null) {
            markUidForRequest(result.getAuthorId());
        }

        parseAttaches(result, message);

        return result;
    }

    private void parseAttaches(Message msg, JSONObject message) {
        try {
            if (message.has("geo")) {
                JSONObject geo = message.getJSONObject("geo");
                String rawLoc = geo.getString("coordinates");
                String[] loc = rawLoc.split(" ");
                msg.setLocation(new Pair<Double, Double>(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])));
            }
            if (message.has("attachments")) {
                msg.setAttachmentPack(new AttachmentPack(message.getJSONArray("attachments")));
            }
            if (message.has("fwd_messages")) {
                JSONArray fwdMsgs = message.getJSONArray("fwd_messages");
                ArrayList<ForwardedMsg> msgs = new ArrayList<ForwardedMsg>();
                for (int i = 0; i < fwdMsgs.length(); i++) {
                    Object iObj = fwdMsgs.get(i);
                    if (iObj instanceof JSONObject) {
                        ForwardedMsg fwdMsg = new ForwardedMsg((JSONObject) iObj);
                        msgs.add(fwdMsg);
                        if (UserStorageFactory.getInstance().getUserStorage().getUser(fwdMsg.getAuthorId(), true) == null) {
                            markUidForRequest(fwdMsg.getAuthorId());
                        }
                    }
                }
                msg.setForwarded(msgs);
            }
        } catch (Throwable e) {
        }
    }

}
