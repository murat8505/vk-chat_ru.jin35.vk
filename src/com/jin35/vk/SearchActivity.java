package com.jin35.vk;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.SearchAdapter;

public class SearchActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        final Adapter<?> adapter = new SearchAdapter(this);
        getListView().setAdapter(adapter);

        ((TextView) findViewById(R.id.top_bar_tv)).setText(R.string.search_top_text);
    }
}
