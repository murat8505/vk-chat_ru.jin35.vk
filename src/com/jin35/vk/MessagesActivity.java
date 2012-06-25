package com.jin35.vk;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.MessagesAdapter;

public class MessagesActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        Adapter<?> adapter = new MessagesAdapter(this);
        getListView().setAdapter(adapter);
        ((TextView) findViewById(R.id.top_bar_tv)).setText(R.string.messages_top_text);
    }
}
