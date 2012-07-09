package com.jin35.vk.adapters;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.model.Message;
import com.jin35.vk.utils.TimeUtils;

public class ConversationOutListItem extends ConversationListItem {

    public ConversationOutListItem(Adapter<?> adapter, Message object) {
        super(adapter, object);
    }

    @Override
    public int getViewId() {
        return R.layout.conversation_out_list_item;
    }

    @Override
    public void updateView(View view) {
        ((TextView) view.findViewById(R.id.time_tv)).setText(TimeUtils.getMessageTime(view.getContext(), getObject().getTime()));
        addContent(view);
        ImageView sendingLoader = (ImageView) view.findViewById(R.id.loader_iv);
        if (!getObject().isRead()) {
            view.setBackgroundColor(0x33000000);
            view.findViewById(R.id.msg_send_flag).setVisibility(View.VISIBLE);
            ImageView sent = (ImageView) view.findViewById(R.id.msg_sent_iv);
            if (getObject().isSent()) {
                sent.setVisibility(View.VISIBLE);
                sendingLoader.setVisibility(View.GONE);
                ((AnimationDrawable) sendingLoader.getDrawable()).stop();
            } else {
                sent.setVisibility(View.INVISIBLE);
                sendingLoader.setVisibility(View.VISIBLE);
                ((AnimationDrawable) sendingLoader.getDrawable()).start();
            }
        } else {
            ((AnimationDrawable) sendingLoader.getDrawable()).stop();
            view.findViewById(R.id.msg_send_flag).setVisibility(View.INVISIBLE);
            view.setBackgroundColor(0x00000000);
        }
        super.updateView(view);
    }
}
