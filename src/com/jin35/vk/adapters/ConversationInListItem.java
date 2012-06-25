package com.jin35.vk.adapters;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.TimeUtils;
import com.jin35.vk.model.Message;

public class ConversationInListItem extends ConversationListItem {

    public ConversationInListItem(Message object) {
        super(object);
    }

    @Override
    public int getViewId() {
        return R.layout.conversation_in_list_item;
    }

    @Override
    public void updateView(View view) {
        super.updateView(view);
        view.findViewById(R.id.photo_iv).setVisibility(View.GONE);// TODO
        ((TextView) view.findViewById(R.id.time_tv)).setText(TimeUtils.getMessageTime(view.getContext(), getObject().getTime()));
        ((TextView) view.findViewById(R.id.msg_content_tv)).setText(Html.fromHtml(getObject().getText()));// TODO
    }

}
