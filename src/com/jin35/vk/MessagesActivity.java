package com.jin35.vk;

import android.app.ListActivity;
import android.os.Bundle;

public class MessagesActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        final Adapter<?> adapter = new MessagesAdapter(this);
        getListView().setAdapter(adapter);
    }
}
