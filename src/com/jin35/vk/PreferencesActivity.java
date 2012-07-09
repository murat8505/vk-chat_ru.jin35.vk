package com.jin35.vk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.LongPollServerConnection;

public class PreferencesActivity extends Activity {

    public static final String LOGOUT = "logout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs);

        findViewById(R.id.logout_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO push logout
                // TODO stop long poll
                // TODO clear cache
                Token.getInstance().removeToken();
                getIntent().putExtra(LOGOUT, true);
                finish();
            }
        });

        findViewById(R.id.exit_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    private void stopLongPoll() {
        LongPollServerConnection.getInstance().stopConnection();
    }
}
