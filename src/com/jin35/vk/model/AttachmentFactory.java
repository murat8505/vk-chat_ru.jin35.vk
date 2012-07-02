package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AttachmentFactory {

    public static final String PHOTO_ATTACH_TYPE = "photo";
    public static final String VIDEO_ATTACH_TYPE = "video";
    public static final String AUDIO_ATTACH_TYPE = "audio";
    public static final String DOC_ATTACH_TYPE = "doc";

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
        if (type.equals(PHOTO_ATTACH_TYPE)) {
            return new PhotoAttach(rawAttach.getJSONObject(PHOTO_ATTACH_TYPE));
        } else if (type.equals(VIDEO_ATTACH_TYPE)) {
            return new VideoAttach(rawAttach.getJSONObject(VIDEO_ATTACH_TYPE));
        } else if (type.equals(AUDIO_ATTACH_TYPE)) {
            return new AudioAttach(rawAttach.getJSONObject(AUDIO_ATTACH_TYPE));
        } else if (type.equals(DOC_ATTACH_TYPE)) {
            return new DocAttach(rawAttach.getJSONObject(DOC_ATTACH_TYPE));
        } else {
            return null;
        }
    }
}
