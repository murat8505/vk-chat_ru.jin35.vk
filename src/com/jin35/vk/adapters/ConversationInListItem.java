package com.jin35.vk.adapters;

import android.view.View;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.TimeUtils;
import com.jin35.vk.model.Message;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;

public class ConversationInListItem extends ConversationListItem {

    public ConversationInListItem(Adapter<?> adapter, Message object) {
        super(adapter, object);
    }

    @Override
    public int getViewId() {
        return R.layout.conversation_in_list_item;
    }

    @Override
    public void updateView(View view) {
        if (!getObject().isRead()) {
            getObject().setRead(true);
            BackgroundTasksQueue.getInstance().execute(
                    new DataRequestTask(DataRequestFactory.getInstance().getMarkAsReadRequest(String.valueOf(getObject().getId()))));
            getObject().notifyChanges();
        }

        view.findViewById(R.id.photo_iv).setVisibility(View.GONE);// TODO
        ((TextView) view.findViewById(R.id.time_tv)).setText(TimeUtils.getMessageTime(view.getContext(), getObject().getTime()));
        addContent(view);
        super.updateView(view);
    }
}
