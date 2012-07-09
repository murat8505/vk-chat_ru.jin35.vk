package com.jin35.vk;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.MessagesAdapter;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;

public class MessagesActivity extends ListActivity {

    private static final int SELECT_RECEIVER = 234234;
    private volatile boolean isDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        ViewGroup topPanelHolder = (ViewGroup) findViewById(R.id.top_bar_ll);
        topPanelHolder.removeAllViews();
        topPanelHolder.addView(LayoutInflater.from(this).inflate(R.layout.messages_list_top_bar, topPanelHolder, false));

        findViewById(R.id.compose_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MessagesActivity.this, FriendsActivity.class).putExtra(FriendsActivity.NEED_RETURN_UID_EXTRA, true),
                        SELECT_RECEIVER);
            }
        });

        Adapter<?> adapter = new MessagesAdapter(this);
        getListView().setAdapter(adapter);
        ((TextView) findViewById(R.id.top_bar_tv)).setText(R.string.messages_top_text);

        getListView().setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount + 2 >= totalItemCount && !isDownloading && MessageStorage.getInstance().hasMoreDialogs()) {
                    isDownloading = true;
                    BackgroundTasksQueue.getInstance()
                            .execute(
                                    new DataRequestTask(DataRequestFactory.getInstance().getDialogsRequest(20,
                                            MessageStorage.getInstance().getDownloadedDialogCount())) {
                                        @Override
                                        public void onError() {
                                            isDownloading = false;
                                        }

                                        @Override
                                        public void onSuccess(Object result) {
                                            isDownloading = false;
                                        }
                                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_RECEIVER && resultCode == RESULT_OK) {
            Long uid = data.getLongExtra(FriendsActivity.UID_EXTRA, -1);
            if (uid > 0) {
                ConversationActivity.start(this, uid);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
