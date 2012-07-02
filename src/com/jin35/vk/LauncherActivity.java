package com.jin35.vk;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.VKRequestFactory;

public class LauncherActivity extends Activity {

    private static final long MIN_LAUNCHER_TIME = 500;
    private static final long MAX_LAUNCHER_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        Token.init(this);
        DB.init(this);
        NotificationCenter.init(new Handler());

        final Thread launcherThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // try {
                // Map<String, String> params = new HashMap<String, String>();
                // String code =
                // "var a=u" +
                // "var b=a@.uid;" +
                // "var i =0" +
                // "while(i<b.length){" +
                // "i=i+1;" +
                // "var c=API.messages.getHistory(\"uid\",)" +
                // "}";
                // params.put("code", code);
                // JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("execute", params);
                // System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
                // System.out.println(response);
                // } catch (Throwable e) {
                // e.printStackTrace();
                // }

                long startTime = System.currentTimeMillis();
                checkToken();
                while (System.currentTimeMillis() - startTime < MIN_LAUNCHER_TIME) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (!TextUtils.isEmpty(Token.getInstance().getToken())) {
                    System.out.println("starting activity");
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
                System.out.println("too long :(");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ImageView loginLoader = (ImageView) findViewById(R.id.loader_iv);
                        loginLoader.setVisibility(View.VISIBLE);
                        ((AnimationDrawable) loginLoader.getDrawable()).start();
                    }
                });

                if (launcherThread != null && launcherThread.isAlive() && !launcherThread.isInterrupted()) {
                    System.out.println("interrupt thread");
                    launcherThread.interrupt();
                }
            }
        }, MAX_LAUNCHER_TIME);
    }

    private void checkToken() {
        if (!TextUtils.isEmpty(Token.getInstance().getToken())) {
            try {
                JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("isAppUser", null, MIN_LAUNCHER_TIME);
                if (response.has(IDataRequest.responseParam)) {
                    if (response.getInt(IDataRequest.responseParam) != 1) {
                        System.out.println("check token - user does'n allow app");
                        Token.getInstance().setNewToken("");
                    } else {
                        System.out.println("check token - OK");
                        DB.getInstance().cacheUsers();
                        System.out.println("check token - cache users - OK");
                        DB.getInstance().cacheMessages();
                        System.out.println("check token - cache messages - OK");
                    }
                } else if (response.has("error")) {
                    System.out.println("check token - error: " + response);
                }
            } catch (Throwable e) {
                System.out.println("check token - fatal error");
                e.printStackTrace();
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
