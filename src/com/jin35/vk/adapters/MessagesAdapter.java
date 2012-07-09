package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;

import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public class MessagesAdapter extends Adapter<IListItem> {
    public MessagesAdapter(ListActivity a) {
        super(a);
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_MESSAGES;
    }

    @Override
    protected List<IListItem> getList() {
        List<Message> messages = MessageStorage.getInstance().getLastMessages();
        synchronized (messages) {
            Collections.sort(messages, new Comparator<Message>() {
                @Override
                public int compare(Message lhs, Message rhs) {
                    return rhs.getTime().compareTo(lhs.getTime());
                }
            });
            List<IListItem> result = new ArrayList<IListItem>();
            for (Message msg : messages) {
                if (msg instanceof ChatMessage) {
                    result.add(new ChatMessageListItem((ChatMessage) msg, activity));
                } else if (msg.isIncome()) {
                    result.add(new MessageListItem(msg, activity));
                } else {
                    result.add(new OutMessageListItem(msg, activity));
                }
            }

            if (MessageStorage.getInstance().hasMoreDialogs()) {
                result.add(new LoaderListItem());
            }
            return result;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof LoaderListItem) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
