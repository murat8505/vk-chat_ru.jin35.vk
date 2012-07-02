package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.media.MediaPlayer;
import android.view.View;

import com.jin35.vk.R;
import com.jin35.vk.adapters.AudioViewStorage;

public class AudioAttach extends Attachment {

    private final String title;
    private final String performer;
    private final String url;
    private final int duration;

    private MediaPlayer player;

    public AudioAttach(JSONObject rawAttachmentParams) throws JSONException {
        super(rawAttachmentParams, "aid");
        url = rawAttachmentParams.getString("url");
        performer = rawAttachmentParams.getString("performer");
        title = rawAttachmentParams.getString("title");
        duration = rawAttachmentParams.getInt("duration");
    }

    @Override
    String getType() {
        return AttachmentFactory.AUDIO_ATTACH_TYPE;
    }

    @Override
    int getSmallIconId() {
        return R.drawable.ic_msg_attach_audio;
    }

    public String getTitle() {
        return title;
    }

    public String getPerformer() {
        return performer;
    }

    public String getUrl() {
        return url;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public int getConversationViewId() {
        return R.layout.audio_conversation_content;
    }

    @Override
    public void fillConversationView(View v) {
        AudioViewStorage.getInstance().fillView(this, v);
    }

}
