package com.jin35.vk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

public abstract class Attachment extends ModelObject implements Serializable {

    private static final long serialVersionUID = 6996385872908102970L;
    private final long ownerId;

    protected Attachment(JSONObject rawAttachmentParams, String uidField) throws JSONException {
        super(rawAttachmentParams.getLong(uidField));
        ownerId = rawAttachmentParams.getLong("owner_id");
    }

    abstract String getType();

    abstract int getSmallIconId();

    public abstract int getConversationViewId();

    public abstract void fillConversationView(View v);

    public long getOwnerId() {
        return ownerId;
    }

}
