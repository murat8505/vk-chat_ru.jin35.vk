package com.jin35.vk;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.VKRequestFactory;

public class LauncherActivity extends Activity {

    private static final long LOADER_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        SystemServices.init(this, false);

        final Thread launcherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                checkToken();
                while (System.currentTimeMillis() - startTime < LOADER_TIME) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (!TextUtils.isEmpty(Token.getInstance().getToken())) {
                    startActivityForResult(new Intent(LauncherActivity.this, VkChatActivity.class), 0);
                } else {
                    startActivityForResult(new Intent(LauncherActivity.this, LoginActivity.class), 0);
                }
            }
        }, "launcher");
        launcherThread.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ImageView loginLoader = (ImageView) findViewById(R.id.loader_iv);
                        loginLoader.setVisibility(View.VISIBLE);
                        ((AnimationDrawable) loginLoader.getDrawable()).start();
                    }
                });
            }
        }, LOADER_TIME);
    }

    private void checkToken() {
        if (!TextUtils.isEmpty(Token.getInstance().getToken())) {
            try {
                JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("isAppUser", null, LOADER_TIME);
                if (response.has(IDataRequest.responseParam)) {
                    if (response.getInt(IDataRequest.responseParam) != 1) {
                        Token.getInstance().removeToken();
                    }
                } else if (response.has("error")) {
                }
            } catch (Throwable e) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || !data.hasExtra(PreferencesActivity.LOGOUT)) {
            finish();
        } else {// нажали кнопку "логаут"
            // обнулить данные??
            startActivityForResult(new Intent(LauncherActivity.this, LoginActivity.class), 0);
        }
    }
}
