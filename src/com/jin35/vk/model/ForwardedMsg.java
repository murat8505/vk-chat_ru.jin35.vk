package com.jin35.vk.model;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class ForwardedMsg implements Serializable {

    private static final long serialVersionUID = -3523785936813869098L;

    private final long id;
    private final String msgText;
    private final long authorId;
    private final Date time;

    private boolean hasFrw;
    private boolean hasLoc;
    private AttachmentPack attaches;

    public ForwardedMsg(long id, String msgText, long authorId, Date time) {
        this.id = id;
        this.msgText = msgText;
        this.authorId = authorId;
        this.time = time;
    }

    public ForwardedMsg(JSONObject rawFwdMsg) throws JSONException {
        id = -1;
        authorId = rawFwdMsg.getLong("uid");
        msgText = rawFwdMsg.getString("body");
        time = new Date(rawFwdMsg.getLong("date") * 1000);
        if (rawFwdMsg.has("geo")) {
            hasLoc = true;
        }
        if (rawFwdMsg.has("fwd_messages")) {
            hasFrw = true;
        }
        if (rawFwdMsg.has("attachments")) {
            attaches = new AttachmentPack(rawFwdMsg.getJSONArray("attachments"));
        }
    }

    public ForwardedMsg(Message msg) {
        id = msg.id;
        msgText = msg.getText();
        authorId = msg.getCorrespondentId();
        time = msg.getTime();
        attaches = msg.getAttachmentPack();
        hasFrw = msg.hasFwd();
        hasLoc = msg.hasLoc();
    }

    public long getId() {
        return id;
    }

    public boolean isHasFrw() {
        return hasFrw;
    }

    public boolean isHasLoc() {
        return hasLoc;
    }

    public AttachmentPack getAttaches() {
        return attaches;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getMsgText() {
        return msgText;
    }

    public Date getTime() {
        return time;
    }

    public boolean hasAnyAttaches() {
        return (attaches != null && attaches.size() > 0) || hasFrw || hasLoc;
    }

}
