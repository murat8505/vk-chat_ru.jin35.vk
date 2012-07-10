package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.LongPollServerConnection;
import com.jin35.vk.net.impl.VKRequestFactory;

public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super("543200980597");
    }

    @Override
    protected void onError(Context arg0, String arg1) {
    }

    @Override
    protected void onMessage(Context context, Intent i) {
        if (LongPollServerConnection.hasInstance()) {// приложение работает
            return;
        }
        if (!PreferencesActivity.pushOn(context)) {
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification();
        String key = i.getStringExtra("collapse_key");
        String message = context.getString(R.string.new_message);
        if (key.equalsIgnoreCase("vkfriend")) {
            message = context.getString(R.string.new_friend_request);
        } else {
        }
        n.setLatestEventInfo(context, context.getString(R.string.app_name), message,
                PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()), 0));
        if (PreferencesActivity.soundOn(context)) {
            n.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.f_4dc7efd744e39);
        }
        n.flags = Notification.FLAG_AUTO_CANCEL;
        n.icon = R.drawable.ic_stat_notify;
        nm.notify(2745, n);

    }

    @Override
    protected void onRegistered(Context arg0, String arg1) {
        if (Token.getInstance() != null) {
            sendRegisterToServer(arg0, arg1);
        }
    }

    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        if (Token.getInstance() != null) {
            sendRegisterToServer(arg0, arg1);
        }
    }

    public static void sendRegisterToServer(final Context context, final String regId) {
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
            @Override
            public void execute() {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", regId);
                    JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("account.registerDevice", params);
                    if (response.has(responseParam)) {
                        GCMRegistrar.setRegisteredOnServer(context, true);
                    }
                } catch (Exception e) {
                }
            }
        }));
    }

    public static void sendUnregisterToServer(final Context context, final String regId) {
        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
            @Override
            public void execute() {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", regId);
                    JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("account.unregisterDevice", params);
                    if (response.has(responseParam)) {
                        GCMRegistrar.setRegisteredOnServer(context, false);
                    }
                } catch (Exception e) {
                }
            }
        }));
    }
}
