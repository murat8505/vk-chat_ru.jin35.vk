package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Attachment extends ModelObject {

    private final long ownerId;

    protected Attachment(JSONObject rawAttachmentParams, String uidField) throws JSONException {
        super(rawAttachmentParams.getLong(uidField));
        ownerId = rawAttachmentParams.getLong("owner_id");
    }

    abstract String getType();

    abstract int getSmallIconId();

    public long getOwnerId() {
        return ownerId;
    }
}
