package com.jin35.vk.net;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;

public class Token {

    private static final String TOKEN_PREF = "token";
    private static final String UID_PREF = "uid";
    private static final String SECURITY_PREFS = "SECURITY_PREFS";

    private final Context context;
    private static Token instance;
    private String currentToken = null;
    private final Timer timer;
    private long currentUid;
    private String currentUserPhoto;

    private Token(Context context) {
        this.context = context;
        currentToken = context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).getString(TOKEN_PREF, null);
        currentUid = context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).getLong(UID_PREF, 0);
        timer = new Timer("minor tasks", true);
    }

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new Token(context);
        }
    }

    public static Token getInstance() {
        return instance;
    }

    public Timer getTimer() {
        return timer;
    }

    public synchronized void startOnlineNotifier() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getMarkAsOnline()));
            }
        }, 0, 600000);// 10 min
    }

    public void setNewToken(long uid, String token) {
        setCurrentUid(uid);
        setNewToken(token);
    }

    public void removeToken() {
        setCurrentUid(0);
        setNewToken("");
    }

    private void setNewToken(String newToken) {
        currentToken = newToken;
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).edit().putString(TOKEN_PREF, newToken).commit();
    }

    public String getToken() {
        return currentToken;
    }

    public long getCurrentUid() {
        return currentUid;
    }

    public String getCurrentUserPhoto() {
        return currentUserPhoto;
    }

    public void setCurrentUserPhoto(String currentUserPhoto) {
        this.currentUserPhoto = currentUserPhoto;
    }

    private void setCurrentUid(long currentUid) {
        this.currentUid = currentUid;
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).edit().putLong(UID_PREF, currentUid).commit();
    }

}
