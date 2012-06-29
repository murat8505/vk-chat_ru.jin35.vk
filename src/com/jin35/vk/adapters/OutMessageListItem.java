package com.jin35.vk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.PhotoStorage;

public class OutMessageListItem extends MessageListItem {

    public OutMessageListItem(Message object, Context context) {
        super(object, context);
    }

    @Override
    protected View getMessageContentView(Context context, ViewGroup root) {
        LinearLayout result = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.simple_out_message_content, root, false);
        ((ImageView) result.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
        TextView tv = (TextView) result.findViewById(R.id.text_tv);
        CharSequence msgText = getObject().getText();
        AttachmentPack attaches = getObject().getAttachmentPack();
        if (attaches != null && attaches.size() > 0) {
            msgText = attaches.addSpans(msgText, context);
        }
        tv.setText(msgText);
        if (!getObject().isRead()) {
            tv.setBackgroundResource(R.drawable.msg_out_unread_bckg);
        }
        return result;
    }
}
