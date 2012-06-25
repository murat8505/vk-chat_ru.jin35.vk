package com.jin35.vk.adapters;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.TimeUtils;
import com.jin35.vk.model.Message;

public class ConversationOutListItem extends ConversationListItem {

    public ConversationOutListItem(Message object) {
        super(object);
    }

    @Override
    public int getViewId() {
        return R.layout.conversation_out_list_item;
    }

    @Override
    public void updateView(View view) {
        super.updateView(view);
        ((TextView) view.findViewById(R.id.time_tv)).setText(TimeUtils.getMessageTime(view.getContext(), getObject().getTime()));
        ((TextView) view.findViewById(R.id.msg_content_tv)).setText(Html.fromHtml(getObject().getText()));// TODO

        if (!getObject().isRead()) {
            view.setBackgroundColor(0x33000000);
            if (getObject().isSent()) {
                view.findViewById(R.id.msg_sent_iv).setVisibility(View.VISIBLE);
            } else {
                // TODO крутилка
                view.findViewById(R.id.msg_sent_iv).setVisibility(View.INVISIBLE);
            }
        } else {
            view.findViewById(R.id.msg_sent_iv).setVisibility(View.INVISIBLE);
            view.setBackgroundColor(0x00000000);
        }

    }
}
