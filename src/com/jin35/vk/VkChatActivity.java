package com.jin35.vk;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.LongPollServerConnection;

public class VkChatActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final View messageTabIndicator = makeTab("messages", R.string.messages, MessagesActivity.class, R.drawable.ic_messages);
        IModelListener unreadMessagesCountListener = new IModelListener() {
            @Override
            public void dataChanged() {
                final TextView notificator = (TextView) messageTabIndicator.findViewById(R.id.notificator_tv);
                final int size = MessageStorage.getInstance().getUreadMessageCount();
                if (size > 0) {
                    notificator.setVisibility(View.VISIBLE);
                    notificator.setText(size > 9 ? "9+" : String.valueOf(size));
                } else {
                    notificator.setVisibility(View.GONE);
                }
            }
        };
        NotificationCenter.getInstance().addModelListener(NotificationCenter.MODEL_MESSAGES, unreadMessagesCountListener);
        unreadMessagesCountListener.dataChanged();
        makeTab("frineds", R.string.friends, FriendsActivity.class, R.drawable.ic_contacts);
        final View searchTabIndicator = makeTab("search", R.string.search, SearchActivity.class, R.drawable.ic_search);
        IModelListener requestsCountListener = new IModelListener() {
            @Override
            public void dataChanged() {
                final TextView notificator = (TextView) searchTabIndicator.findViewById(R.id.notificator_tv);
                final int size = UserStorageFactory.getInstance().getUserStorage().getRequestsCount();
                if (size > 0) {
                    notificator.setVisibility(View.VISIBLE);
                    notificator.setText(size > 9 ? "9+" : String.valueOf(size));
                } else {
                    notificator.setVisibility(View.GONE);
                }
            }
        };
        NotificationCenter.getInstance().addModelListener(NotificationCenter.MODEL_REQUESTS, requestsCountListener);
        requestsCountListener.dataChanged();
        makeTab("prefs", R.string.prefs, PreferencesActivity.class, R.drawable.ic_preferences);

        getTabHost().setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getTabHost().getWindowToken(), 0);
            }
        });
        PhotoStorage.init(getApplicationContext());

        LongPollServerConnection.getInstance();

        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getSuggestionsRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getRequestesRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getFriendsRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getDialogsRequest()));

    }

    private View makeTab(String tag, int textResource, Class<?> intentClass, int imageResource) {
        TabSpec tab = getTabHost().newTabSpec(tag);
        tab.setContent(new Intent(this, intentClass));
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
        TextView indicatorText = ((TextView) tabIndicator.findViewById(R.id.text_tv));
        indicatorText.setText(textResource);
        if (imageResource != 0) {
            ((ImageView) tabIndicator.findViewById(R.id.icon_iv)).setImageResource(imageResource);
        }
        tab.setIndicator(tabIndicator);
        getTabHost().addTab(tab);
        return tabIndicator;
    }

    @Override
    public void finishFromChild(Activity child) {
        setResult(RESULT_OK, child.getIntent());
        super.finishFromChild(child);
    }
}