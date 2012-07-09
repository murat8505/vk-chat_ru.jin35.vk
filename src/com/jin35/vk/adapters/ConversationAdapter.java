package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.View;

import com.jin35.vk.ConversationActivity;
import com.jin35.vk.R;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public class ConversationAdapter extends Adapter<IListItem> {

    private final long chatId;

    public ConversationAdapter(ConversationActivity activity, long uid) {
        super(activity, false);
        this.chatId = uid;
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
        return 0;
    }

    @Override
    protected void subscribeListener() {
        NotificationCenter.getInstance().addConversationListener(getChatId(), listener);
    }

    protected long getChatId() {
        return chatId;
    }

    @Override
    protected List<IListItem> getList() {
        List<IListItem> result = new ArrayList<IListItem>();

        if (MessageStorage.getInstance().hasMoreMessagesWithUser(getChatId())) {
            result.add(new LoaderListItem());
        }

        List<Message> messagesWithUser = MessageStorage.getInstance().getMessagesWithUser(getChatId());
        synchronized (messagesWithUser) {
            Collections.sort(messagesWithUser, Message.getDescendingTimeComparator());
            for (Message msg : messagesWithUser) {
                if (msg.isDeleting()) {
                    continue;
                }
                if (msg.isIncome()) {
                    result.add(new ConversationInListItem(this, msg));
                } else {
                    result.add(new ConversationOutListItem(this, msg));
                }
            }
        }
        if (MessageStorage.getInstance().isUserTyping(getChatId())) {
            result.add(new TypingListItem());
        }
        // TODO add "was online" item
        return result;
    }

    private class TypingListItem implements IListItem {
        @Override
        public long getId() {
            return 0;
        }

        @Override
        public int getViewId() {
            return R.layout.typing_list_item;
        }

        @Override
        public void updateView(View view) {
        }

        @Override
        public boolean needListener() {
            return false;
        }

        @Override
        public void subsribeListenerForObject(IModelListener listener) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

    }
}
