package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ImageSpan;

public class AttachmentPack {

    private final List<Attachment> attaches = new ArrayList<Attachment>();
    private final Map<String, Attachment> attachesTypes = new HashMap<String, Attachment>();

    public AttachmentPack(JSONArray rawData) throws JSONException {
        for (int i = 0; i < rawData.length(); i++) {
            JSONObject oneAttach = rawData.getJSONObject(i);
            Attachment a = AttachmentFactory.getInstance().getAttachment(oneAttach);
            if (a != null) {
                attaches.add(a);
                String type = a.getType();
                if (!attachesTypes.containsKey(type)) {
                    attachesTypes.put(type, a);
                }
            }
        }
    }

    public List<Attachment> getAttaches() {
        return attaches;
    }

    public int size() {
        return attaches.size();
    }

    public CharSequence addSpans(CharSequence msgText, Context context) {
        SpannableString result = new SpannableString(msgText);
        for (Attachment a : attachesTypes.values()) {
            ((SpannableString) msgText).setSpan(new ImageSpan(context, a.getSmallIconId(), ImageSpan.ALIGN_BASELINE), 0, 0,
                    SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return result;
    }
}
