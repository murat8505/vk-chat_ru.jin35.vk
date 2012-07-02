package com.jin35.vk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.jin35.vk.R;

public class AttachmentPack implements Iterable<Attachment>, Serializable {

    private static final long serialVersionUID = 2321898805959100172L;
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

    public static CharSequence addSpans(CharSequence msgText, Context context, boolean addFrwSpan, boolean addLocSpan, AttachmentPack attaches) {
        int spanCount = (attaches == null ? 0 : attaches.attachesTypes.size()) + (addFrwSpan ? 1 : 0) + (addLocSpan ? 1 : 0);
        for (int i = 0; i < spanCount; i++) {
            msgText = "  " + msgText;
        }
        int nextSpanIndex = 0;
        SpannableString result = new SpannableString(msgText);
        if (addLocSpan) {
            addImageSpan(result, context, R.drawable.ic_msg_attach_loc, nextSpanIndex);
            nextSpanIndex += 2;
        }
        if (attaches != null) {
            for (Attachment a : attaches.attachesTypes.values()) {
                addImageSpan(result, context, a.getSmallIconId(), nextSpanIndex);
                nextSpanIndex += 2;
            }
        }
        if (addFrwSpan) {
            addImageSpan(result, context, R.drawable.ic_msg_attach_fwd, nextSpanIndex);
            nextSpanIndex += 2;
        }
        return result;
    }

    private static void addImageSpan(SpannableString string, Context context, int drawableId, int startIndex) {
        ImageSpan span = new ImageSpan(context, drawableId, ImageSpan.ALIGN_BASELINE);
        string.setSpan(span, startIndex, startIndex + 1, SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
    }

    @Override
    public Iterator<Attachment> iterator() {
        return attaches.listIterator();
    }
}
