package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.R;

public class PhotoAttach extends Attachment {

    private final String smallUrl;

    public PhotoAttach(JSONObject rawAttachmentParams) throws JSONException {
        super(rawAttachmentParams, "pid");
        smallUrl = rawAttachmentParams.getString("src");
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    @Override
    String getType() {
        return "photo";
    }

    @Override
    int getSmallIconId() {
        return R.drawable.ic_msg_attach_photo;
    }

}
