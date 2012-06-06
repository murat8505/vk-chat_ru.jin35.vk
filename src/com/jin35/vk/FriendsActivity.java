package com.jin35.vk;

import android.app.ListActivity;
import android.os.Bundle;

public class FriendsActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        final Adapter<?> adapter = new AllFriendsAdapter(this);
        getListView().setAdapter(adapter);
        // NotificationCenter.getInstance().addModelListener(NotificationCenter.FRIENDS, new IModelListener() {
        // @Override
        // public void dataChanged() {
        // runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // adapter.notifyDataSetChanged();
        // }
        // });
        // }
        // });
    }
}
