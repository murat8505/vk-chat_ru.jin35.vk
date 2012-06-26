package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public class MessagesAdapter extends Adapter<MessageListItem> {
    public MessagesAdapter(ListActivity a) {
        super(a);
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_MESSAGES;
    }

    @Override
    protected List<MessageListItem> getList() {
        List<Message> messages = MessageStorage.getInstance().getLastMessages();
        synchronized (messages) {
            Collections.sort(messages, new Comparator<Message>() {
                @Override
                public int compare(Message lhs, Message rhs) {
                    return rhs.getTime().compareTo(lhs.getTime());
                }
            });
            List<MessageListItem> result = new ArrayList<MessageListItem>();
            for (Message msg : messages) {
                result.add(new MessageListItem(msg, activity));
            }
            return result;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
