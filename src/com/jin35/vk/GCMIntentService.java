package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.VKRequestFactory;

public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super("543200980597");
    }

    @Override
    protected void onError(Context arg0, String arg1) {
        System.out.println("onError");
    }

    @Override
    protected void onMessage(Context arg0, Intent arg1) {
        // TODO show notification
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
                    } else {
                        System.out.println("error in unregister: " + responseParam);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
