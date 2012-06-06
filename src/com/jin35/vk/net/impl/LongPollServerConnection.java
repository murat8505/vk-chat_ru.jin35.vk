package com.jin35.vk.net.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorage;

public class LongPollServerConnection {

    private static final int USER_ONLINE_UPDT_CODE = 8;
    private static final int USER_OFFLINE_UPDT_CODE = 9;

    private LongPollServerParams params;
    private Thread longPollConnectionThread;

    public LongPollServerConnection() {
        BackgroundTasksQueue.getInstance().execute(new GetParamsTask());
    }

    private void raiseThread() {
        if (longPollConnectionThread != null) {
            System.out.println("thread already running!");
            return;
        }
        longPollConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("thread started");
                while (true) {
                    String url = "http://" + params.server + "?act=a_check&key=" + params.key + "&ts=" + params.ts + "&wait=25&mode=2";
                    try {
                        URLConnection conn = new URL(url).openConnection();
                        String answer = "";
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            answer = answer.concat(inputLine);
                        }
                        in.close();
                        JSONObject jsonAnswer = new JSONObject(answer);

                        if (jsonAnswer.has("failed")) {
                            System.out.println("lps answer - failed");
                            BackgroundTasksQueue.getInstance().execute(new GetParamsTask());
                            synchronized (params) {
                                params.wait();
                            }
                        } else {
                            params.ts = jsonAnswer.getLong("ts");
                            System.out.println("lps answer - result: new ts = " + params.ts);
                            JSONArray updates = jsonAnswer.getJSONArray("updates");
                            for (int i = 0; i < updates.length(); i++) {
                                JSONArray updateDescr = updates.getJSONArray(i);
                                parseUpdate(updateDescr);
                            }
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "long poll connection");
        System.out.println("starting thread...");
        longPollConnectionThread.start();
    }

    private void parseUpdate(JSONArray update) {
        try {
            int updateCode = update.getInt(0);
            switch (updateCode) {
            case USER_OFFLINE_UPDT_CODE: {
                long uid = update.getLong(1);
                UserInfo user = UserStorage.getInstance().getUser(uid);
                user.setOnline(false);
                user.notifyChanges();
                break;
            }
            case USER_ONLINE_UPDT_CODE: {
                long uid = update.getLong(1);
                UserInfo user = UserStorage.getInstance().getUser(uid);
                user.setOnline(true);
                user.notifyChanges();
                break;
            }
            default:
                break;
            }
        } catch (Throwable e) {
        }
    }

    private void setNewParams(LongPollServerParams params) {
        System.out.println("start set new params, thread: " + Thread.currentThread().getName());
        if (this.params == null) {
            this.params = params;
            System.out.println("params == null!");
        } else {
            synchronized (this.params) {
                System.out.println("inside sync block");
                this.params = params;
                this.params.notifyAll();
            }
        }
        System.out.println("return");
    }

    private class GetParamsTask extends BackgroundTask<LongPollServerParams> {
        private GetParamsTask() {
            super(1);
        }

        @Override
        public LongPollServerParams execute() throws Throwable {
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequest("messages.getLongPollServer", null);
            if (answer.has("response")) {
                JSONObject response = answer.getJSONObject("response");
                try {
                    String key = response.getString("key");
                    String server = response.getString("server");
                    long ts = response.getLong("ts");
                    return new LongPollServerParams(server, key, ts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public void onSuccess(LongPollServerParams result) {
            if (result != null) {
                setNewParams(result);
                raiseThread();
            }
        }

        @Override
        public void onError() {
        }
    }

    private class LongPollServerParams {
        private final String server;
        private final String key;
        private long ts;

        private LongPollServerParams(String server, String key, long ts) {
            this.server = server;
            this.key = key;
            this.ts = ts;
        }
    }

}
