package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.jin35.vk.R;

public class PhotoAttach extends Attachment {

    private final String smallUrl;

    private final String bigUrl;

    public PhotoAttach(JSONObject rawAttachmentParams) throws JSONException {
        super(rawAttachmentParams, "pid");
        smallUrl = rawAttachmentParams.getString("src");
        if (rawAttachmentParams.has("src_xxxbig")) {
            bigUrl = rawAttachmentParams.getString("src_xxxbig");
        } else if (rawAttachmentParams.has("src_xxbig")) {
            bigUrl = rawAttachmentParams.getString("src_xxbig");
        } else if (rawAttachmentParams.has("src_xbig")) {
            bigUrl = rawAttachmentParams.getString("src_xbig");
        } else {
            bigUrl = rawAttachmentParams.getString("src_big");
        }
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
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(bigUrl));
                v.getContext().startActivity(i);
            }
        });
    }
}
