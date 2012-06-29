package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AttachmentFactory {

    public static final String PHOTO_ATTACH_TYPE = "photo";

    private static AttachmentFactory instance;

    private AttachmentFactory() {
    }

    public synchronized static AttachmentFactory getInstance() {
        if (instance == null) {
            instance = new AttachmentFactory();
        }
        return instance;
    }

    public Attachment getAttachment(JSONObject rawAttach) throws JSONException {
        String type = rawAttach.getString("type");
        if (type == PHOTO_ATTACH_TYPE) {
            return new PhotoAttach(rawAttach.getJSONObject(PHOTO_ATTACH_TYPE));
        } else {
            return null;
        }
    }
}
