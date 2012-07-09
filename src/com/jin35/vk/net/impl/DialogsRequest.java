package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;

public class DialogsRequest extends BaseMessageRequest {

    private final int limit;
    private final int offset;

    public DialogsRequest(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public void execute() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            String code = "var dialogs=API.messages.getDialogs({\"offset\":" + offset + ",\"count\":" + limit + "});" + //
                    "var uids=dialogs@.uid;" + //
                    "var cids=dialogs@.chat_id;" + //
                    "var i=uids.length;" + //
                    "var result=[];" + //
                    "while(i>0){" + //
                    "i=i-1;" + //
                    "var c=0;" + //
                    "c=c+cids[i];" + //
                    "if(c>0){" + //
                    "result=result+[{\"chatid\":cids[i], \"msgs\":API.messages.getHistory({\"chat_id\":cids[i]})}];" + //
                    "}else{" + //
                    "result=result+[{\"corrid\":uids[i], \"msgs\":API.messages.getHistory({\"uid\":uids[i]})}];" + //
                    "}" + //
                    "}" + //
                    "return result;";//
            params.put("code", code);
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);

            parseMessagesFromResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseMessagesFromResponse(JSONObject response) throws JSONException {
        if (response.has(responseParam)) {
            JSONArray array = response.getJSONArray(responseParam);
            List<Message> msgs = new ArrayList<Message>();
            int dialogsCount = 0;
            Map<Long, Integer> messageCounts = new HashMap<Long, Integer>();
            Map<Long, Integer> chatMessageCounts = new HashMap<Long, Integer>();
            for (int i = 0; i < array.length(); i++) {
                if (!(array.get(i) instanceof JSONObject)) {
                    continue;
                }
                JSONObject dialog = array.getJSONObject(i);
                if (dialog.has("corrid")) {
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
                        Message msg = parseOneMessage(oneMessage, correspondentId, false);
                        msg.notifyChanges();
                        msgs.add(msg);
                        msgCount++;
                    }
                    messageCounts.put(correspondentId, msgCount);
                } else if (dialog.has("chatid")) {
                    if (dialog.get("chatid") == JSONObject.NULL) {
                        continue;
                    }
                    Long chatId = dialog.getLong("chatid");
                    JSONArray dialogMsgs = dialog.getJSONArray("msgs");
                    dialogsCount++;
                    int msgCount = 0;
                    for (int j = 0; j < dialogMsgs.length(); j++) {
                        if (!(dialogMsgs.get(j) instanceof JSONObject)) {
                            continue;
                        }
                        JSONObject oneMessage = dialogMsgs.getJSONObject(j);
                        ChatMessage msg = (ChatMessage) parseOneMessage(oneMessage, chatId, true);
                        msg.notifyChanges();
                        msgs.add(msg);
                        msgCount++;
                    }
                    chatMessageCounts.put(chatId, msgCount);
                }
            }

            requestAdditionalInfo();

            MessageStorage.getInstance().addMessages(msgs);
            MessageStorage.getInstance().setDownloadedDialogCount(messageCounts.size() + chatMessageCounts.size());
            for (Entry<Long, Integer> e : messageCounts.entrySet()) {
                MessageStorage.getInstance().setMessagesWithUserCount(e.getKey(), e.getValue());
            }
            for (Entry<Long, Integer> e : chatMessageCounts.entrySet()) {
                MessageStorage.getInstance().setChatMessagesCount(e.getKey(), e.getValue());
            }
            MessageStorage.getInstance().dump();
        }
    }
}
