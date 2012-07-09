package com.jin35.vk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jin35.vk.adapters.ChatConversationAdapter;
import com.jin35.vk.adapters.ConversationAdapter;
import com.jin35.vk.model.Chat;
import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.ChatStorage;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;

public class ChatConversationActivity extends ConversationActivity {
    private static final String CHAT_ID_EXTRA = "chat id";
    private long chatId;

    public static void start(long chatId, Context context) {
        Intent i = new Intent(context, ChatConversationActivity.class);
        i.putExtra(CHAT_ID_EXTRA, chatId);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chatId = getIntent().getLongExtra(CHAT_ID_EXTRA, 0);
        super.onCreate(savedInstanceState);
        findViewById(R.id.edit_chat_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    @Override
    protected ConversationAdapter getAdapter() {
        return new ChatConversationAdapter(this, chatId);
    }

    @Override
    protected IDataRequest getMoreMessagesRequest() {
        // TODO Auto-generated method stub
        return super.getMoreMessagesRequest();
    }

    @Override
    protected Message getNewMessageForSending(String messageText) {
        return new ChatMessage(Message.getUniqueTempId(), chatId, messageText, new Date(System.currentTimeMillis()), false, Token.getInstance().getCurrentUid());
    }

    @Override
    protected long getTopPanelObjectId() {
        return chatId;
    }

    @Override
    protected boolean hasMoreMessages() {
        return MessageStorage.getInstance().hasMoreChatMessages(chatId);
    }

    @Override
    protected int getMessageCount() {
        return MessageStorage.getInstance().getDownloadedChatMessageCount(chatId);
    }

    @Override
    protected List<Message> getAllMessages() {
        return new ArrayList<Message>(MessageStorage.getInstance().getMessagesFromChat(chatId));
    }

    @Override
    protected int getTopPanelLayoutId() {
        return R.layout.chat_conversation_top_panel;
    }

    @Override
    protected void updateMainTopPanel() {
        Chat chat = ChatStorage.getInstance().getChat(chatId);
        Button btn = ((Button) findViewById(R.id.edit_chat_btn));
        TextView textView = (TextView) findViewById(R.id.name_tv);
        if (chat == null) {
            textView.setText(R.string.not_dowanloaded_name);
            btn.setText("");
        } else {
            textView.setText(chat.getChatName());
            btn.setText(String.valueOf(chat.getUsers().size()));
        }

    }
}
