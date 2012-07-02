package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

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
        return AttachmentFactory.PHOTO_ATTACH_TYPE;
    }

    @Override
    int getSmallIconId() {
        return R.drawable.ic_msg_attach_photo;
    }

    @Override
    public int getConversationViewId() {
        return R.layout.photo_converation_content;
    }

    @Override
    public void fillConversationView(View v) {
        ImageView imageView = (ImageView) v;
        Drawable image = PhotoStorage.getInstance().getPhoto(smallUrl, getId(), false, false);
        imageView.setScaleType(ScaleType.CENTER_CROP);
        if (image != null) {
            imageView.setImageDrawable(image);
        } else {
            imageView.setImageResource(R.drawable.contact_no_photo);
        }
    }
}
