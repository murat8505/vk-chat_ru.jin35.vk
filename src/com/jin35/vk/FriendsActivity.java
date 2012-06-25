package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.FriendsAdapter;
import com.jin35.vk.adapters.OnlineFriendsAdapter;

public class FriendsActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        ViewGroup topPanelHolder = (ViewGroup) findViewById(R.id.top_bar_ll);
        topPanelHolder.removeAllViews();
        topPanelHolder.addView(LayoutInflater.from(this).inflate(R.layout.friend_list_type_choice, topPanelHolder, false));

        final Map<Button, Adapter<?>> adapters = new HashMap<Button, Adapter<?>>(3);

        adapters.put((Button) findViewById(R.id.all_friends_btn), new FriendsAdapter(this));
        adapters.put((Button) findViewById(R.id.online_friends_btn), new OnlineFriendsAdapter(this));

        for (Button btn : adapters.keySet()) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (Button btn : adapters.keySet()) {
                        if (v.equals(btn)) {
                            btn.setTextColor(0xFFFFFFFF);
                        } else {
                            btn.setTextColor(0x88FFFFFF);
                        }
                    }
                    Adapter<?> adapter = adapters.get(v);
                    getListView().setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        ((Button) findViewById(R.id.all_friends_btn)).performClick();
    }
}
