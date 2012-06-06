package com.jin35.vk;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost.TabSpec;

import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.LongPollServerConnection;

public class VkChatActivity extends TabActivity {

    private static String token = "7e1e43777e019f7e7e019f7eee7e2cd83677e017e019f7e6a50b218742580fe";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabSpec friendsTab = getTabHost().newTabSpec("friends");
        friendsTab.setContent(new Intent(this, FriendsActivity.class));
        friendsTab.setIndicator("friends");
        getTabHost().addTab(friendsTab);

        TabSpec messagesTab = getTabHost().newTabSpec("messages");
        messagesTab.setContent(new Intent(this, MessagesActivity.class));
        messagesTab.setIndicator("messages");
        getTabHost().addTab(messagesTab);

        Token.setNewToken(token);
        PhotoStorage.init(getApplicationContext());

        new LongPollServerConnection();

        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getFriendsRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getMessagesRequest()));
    }
}