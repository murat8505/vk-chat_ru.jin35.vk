package com.jin35.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.TextView;

import com.jin35.vk.R;

public class DocAttach extends Attachment {

    private final String title;
    private final String ext;
    private final String url;

    public DocAttach(JSONObject rawAttachmentParams) throws JSONException {
        super(rawAttachmentParams, "did");
        title = rawAttachmentParams.getString("title");
        url = rawAttachmentParams.getString("url");
        ext = rawAttachmentParams.getString("ext");
    }

    @Override
    String getType() {
        return AttachmentFactory.DOC_ATTACH_TYPE;
    }

    @Override
    int getSmallIconId() {
        return R.drawable.ic_msg_attach_doc;
    }

    public String getTitle() {
        return title;
    }

    public String getExt() {
        return ext;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int getConversationViewId() {
        // TODO Auto-generated method stub
        return R.layout.simple_conversation_content;
    }

    @Override
    public void fillConversationView(View v) {
        ((TextView) v).setText("doc: " + title + "." + ext);
    }

}
