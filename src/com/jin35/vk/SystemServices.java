package com.jin35.vk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.ICaptchaHandler;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.LongPollServerConnection;
import com.jin35.vk.net.impl.VKRequestFactory;

public class SystemServices {

    public static void init(Activity currentActivity, boolean logged) {
        Token.init(currentActivity);
        DB.init(currentActivity);
        NotificationCenter.init(new Handler());
        PhotoStorage.init(currentActivity);
        VKRequestFactory.init(getHandler(currentActivity), null);

        if (logged) {
            boolean needDataUpdate = true;
            if (UserStorageFactory.getInstance().initUserStorage()) {
                DB.getInstance().cacheUsers();
                postDataRequest();
                needDataUpdate = false;
            }

            if (MessageStorage.init()) {
                DB.getInstance().cacheMessages();
                if (needDataUpdate) {
                    postDataRequest();
                }
            }
            LongPollServerConnection.getInstance();
        }
    }

    private static void postDataRequest() {
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getCurrentUserPhotoRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getSuggestionsRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getRequestesRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getFriendsRequest()));
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getDialogsRequest(20, 0)));
    }

    private static ICaptchaHandler getHandler(final Activity currentActivity) {
        return new ICaptchaHandler() {
            @Override
            public String onCapchaNeeded(String captchaImageUrl) throws InterruptedException {
                SharedPreferences prefs = currentActivity.getSharedPreferences(CapthcaActivity.PREF_NAME, Activity.MODE_PRIVATE);
                prefs.edit().putString(CapthcaActivity.RESULT_PREF_NAME, null).commit();
                Intent i = new Intent(currentActivity, CapthcaActivity.class);
                i.putExtra(CapthcaActivity.IMG_URL_EXTRA, captchaImageUrl);
                currentActivity.startActivity(i);

                while (prefs.getString(CapthcaActivity.RESULT_PREF_NAME, null) == null) {
                    Thread.sleep(100);
                }
                return prefs.getString(CapthcaActivity.RESULT_PREF_NAME, null);
            }
        };
    }
}
