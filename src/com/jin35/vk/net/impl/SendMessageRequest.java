package com.jin35.vk.net.impl;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.ForwardedMsg;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.net.IDataRequest;

public class SendMessageRequest implements IDataRequest {

    private final Message message;
    private final List<Bitmap> attaches;

    public SendMessageRequest(Message message, List<Bitmap> attaches) {
        this.message = message;
        this.attaches = attaches;
    }

    public SendMessageRequest(Message message) {
        this(message, null);
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        if (message instanceof ChatMessage) {
            params.put("chat_id", String.valueOf(message.getCorrespondentId()));
        } else {
            params.put("uid", String.valueOf(message.getCorrespondentId()));
        }
        params.put("message", message.getText());
        params.put("type", "1");

        List<ForwardedMsg> fwr = message.getForwarded();
        if (fwr != null && fwr.size() > 0) {
            String mids = "";
            for (ForwardedMsg frwMsg : fwr) {
                mids = mids.concat(String.valueOf(frwMsg.getId())).concat(",");
            }
            params.put("forward_messages", mids.substring(0, mids.length() - 1));
        }
        if (message.getLocation() != null) {
            params.put("lat", String.valueOf(message.getLocation().first));
            params.put("long", String.valueOf(message.getLocation().second));
        }
        if (attaches != null && attaches.size() > 0) {
            try {
                String pids = "";
                JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("photos.getMessagesUploadServer", null);
                String serverUrl = response.getJSONObject(responseParam).getString("upload_url");
                for (Bitmap attach : attaches) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    attach.compress(CompressFormat.JPEG, 100, baos);
                    JSONObject photoUploadResponse = VKRequestFactory.getInstance().getRequest()
                            .executePost(serverUrl, "photo", "image/png", baos.toByteArray());
                    Map<String, String> paramsForSave = new HashMap<String, String>();
                    paramsForSave.put("server", photoUploadResponse.getString("server"));
                    paramsForSave.put("photo", photoUploadResponse.getString("photo"));
                    paramsForSave.put("hash", photoUploadResponse.getString("hash"));
                    JSONObject photoSaveResponse = VKRequestFactory.getInstance().getRequest()
                            .executeRequestToAPIServer("photos.saveMessagesPhoto", paramsForSave);

                    String photoId = photoSaveResponse.getJSONArray(responseParam).getJSONObject(0).getString("id");

                    pids = pids.concat(photoId).concat(",");
                }
                params.put("attachment", pids.substring(0, pids.length() - 1));
            } catch (Exception e) {
                return;
            }
        }
        // params.put("guid", message.getUnique()); // TODO ?
        try {
            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.send", params);
            long mid = response.getLong(responseParam);
            message.setSent(true);
            MessageStorage.getInstance().updateMsgId(message, mid);
            // messageSent(message.getCorrespondentId(), message.getText(), message.getId(), null, mid, false);
        } catch (Exception e) {
            // TODO try resend?
        }
    }
}
