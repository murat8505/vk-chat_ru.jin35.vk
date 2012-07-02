package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jin35.vk.PlayVideoActivity;
import com.jin35.vk.R;

public class VideoAttach extends Attachment {

    private final String previewUrl;
    private final int duration;

    public VideoAttach(JSONObject rawAttachmentParams) throws JSONException {
        super(rawAttachmentParams, "vid");
        previewUrl = rawAttachmentParams.getString("image");
        duration = rawAttachmentParams.getInt("duration");
    }

    @Override
    String getType() {
        return AttachmentFactory.VIDEO_ATTACH_TYPE;
    }

    @Override
    int getSmallIconId() {
        return R.drawable.ic_msg_attach_video;
    }

    public int getDuration() {
        return duration;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    @Override
    public int getConversationViewId() {
        return R.layout.video_conversation_content;
    }

    @Override
    public void fillConversationView(View v) {
        String durationText = "";
        int hours = duration / 3600;
        if (hours > 0) {
            durationText += hours + ":";
        }
        int minutes = ((duration % 3600) / 60);
        durationText += minutes + ":" + duration % 60;
        ((TextView) v.findViewById(R.id.video_duration_tv)).setText(durationText);
        Drawable preview = PhotoStorage.getInstance().getPhoto(previewUrl, id, false, false);
        ImageView iv = ((ImageView) v.findViewById(R.id.video_preview_iv));
        if (preview != null) {
            iv.setImageDrawable(preview);
        } else {
            iv.setImageDrawable(new ColorDrawable(0xDD777777));
        }
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideoActivity.start(v.getContext(), getOwnerId() + "_" + getId());
            }
        });
    }
}
