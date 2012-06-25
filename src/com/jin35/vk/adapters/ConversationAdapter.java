package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jin35.vk.ConversationActivity;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public class ConversationAdapter extends Adapter<IListItem> {

    private final long uid;

    public ConversationAdapter(ConversationActivity activity, long uid) {
        super(activity, false);
        this.uid = uid;
        onCreate();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof ConversationInListItem) {
            return 0;
        }
        if (getItem(position) instanceof ConversationOutListItem) {
            return 1;
        }
        return 2;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_MESSAGES;
    }

    @Override
    protected List<IListItem> getList() {
        List<IListItem> result = new ArrayList<IListItem>();
        List<Message> messagesWithUser = MessageStorage.getInstance().getMessagesWithUser(uid);
        synchronized (messagesWithUser) {
            Collections.sort(messagesWithUser, Message.getDescendingTimeComparator());
            for (Message msg : messagesWithUser) {
                if (msg.isIncome()) {
                    result.add(new ConversationInListItem(msg));
                } else {
                    result.add(new ConversationOutListItem(msg));
                }
            }
        }
        // TODO add "typing" or "was online" items
        return result;
    }

    @Override
    protected void onDataSetChanged() {
        ((ConversationActivity) activity).getListView().scrollTo(0, getCount() - 1);
    }
}
