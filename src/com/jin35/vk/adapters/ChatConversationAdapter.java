package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jin35.vk.ConversationActivity;
import com.jin35.vk.R;
import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class ChatConversationAdapter extends ConversationAdapter {

    public ChatConversationAdapter(ConversationActivity activity, long chatId) {
        super(activity, chatId);
    }

    @Override
    protected List<IListItem> getList() {
        List<IListItem> result = new ArrayList<IListItem>();

        if (MessageStorage.getInstance().hasUnreadMessagesFromChat(getChatId())) {
            result.add(new LoaderListItem());
        }

        List<ChatMessage> messagesWithUser = MessageStorage.getInstance().getMessagesFromChat(getChatId());
        synchronized (messagesWithUser) {
            Collections.sort(messagesWithUser, Message.getDescendingTimeComparator());
            for (ChatMessage msg : messagesWithUser) {
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
        String names = "";
        for (Long uid : MessageStorage.getInstance().getUsersTyping(getChatId())) {
            UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(uid, true);
            if (user != null) {
                names += user.getName() + ", ";
            }
        }
        if (!TextUtils.isEmpty(names)) {
            result.add(new TypingListItem(names.substring(0, names.length() - 2)));
        }
        return result;
    }

    private class TypingListItem implements IListItem {

        private final String names;

        public TypingListItem(String names) {
            this.names = names;
        }

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
            ((TextView) view).setText(names + view.getContext().getString(R.string.typing_message));
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
