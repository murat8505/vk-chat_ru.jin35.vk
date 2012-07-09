package com.jin35.vk.net.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class LongPollServerConnection {

    private static final int MSG_FLAG_VALUE_UNREAD = 1;
    private static final int MSG_FLAG_VALUE_OUTBOX = 2;
    private static final int MSG_FLAG_VALUE_DELETED = 128;

    private static final int MSG_DELETE_UPDT_CODE = 0;
    private static final int MSG_FLAGS_CHANGED_UPDT_CODE = 1;
    private static final int MSG_FLAG_ADDED_UPDT_CODE = 2;
    private static final int MSG_FLAG_REMOVED_UPDT_CODE = 3;
    private static final int NEW_MSG_UPDT_CODE = 4;
    private static final int USER_ONLINE_UPDT_CODE = 8;
    private static final int USER_OFFLINE_UPDT_CODE = 9;

    private static final int USER_TYPING_UPDT_CODE = 61;

    private volatile LongPollServerParams params;
    private Thread longPollConnectionThread;
    private volatile boolean stopped;

    private static LongPollServerConnection instance;

    private LongPollServerConnection() {
        stopped = false;
        BackgroundTasksQueue.getInstance().execute(new GetParamsTask());
    }

    public static synchronized LongPollServerConnection getInstance() {
        if (instance == null) {
            instance = new LongPollServerConnection();
        }
        return instance;
    }

    public void stopConnection() {
        stopped = true;
        if (longPollConnectionThread != null && longPollConnectionThread.isAlive() && !longPollConnectionThread.isInterrupted()) {
            longPollConnectionThread.interrupt();
        }
        params = null;
        instance = null;
    }

    private void raiseThread() {
        if (longPollConnectionThread != null || stopped) {
            return;
        }
        longPollConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String url = "http://" + params.server + "?act=a_check&key=" + params.key + "&ts=" + params.ts + "&wait=25&mode=2";
                    try {
                        JSONObject jsonAnswer = VKRequestFactory.getInstance().getRequest().executeRequest(url);

                        if (jsonAnswer.has("failed")) {
                            BackgroundTasksQueue.getInstance().execute(new GetParamsTask());
                            synchronized (params) {
                                params.wait();
                            }
                        } else {
                            params.ts = jsonAnswer.getLong("ts");
                            JSONArray updates = jsonAnswer.getJSONArray("updates");
                            for (int i = 0; i < updates.length(); i++) {
                                JSONArray updateDescr = updates.getJSONArray(i);
                                parseUpdate(updateDescr);
                            }
                        }

                    } catch (InterruptedException e) {
                        break;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "long poll connection");
        longPollConnectionThread.start();
    }

    private void parseUpdate(JSONArray update) {
        try {
            System.out.println("update: " + update);
            int updateCode = update.getInt(0);
            switch (updateCode) {
            case USER_OFFLINE_UPDT_CODE:
            case USER_ONLINE_UPDT_CODE: {
                long uid = Math.abs(update.getLong(1));
                UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(uid, false);
                user.setOnline(updateCode == USER_ONLINE_UPDT_CODE);
                System.out.println("set online for user: " + user.getId() + ", " + user.isOnline());
                user.notifyChanges();
                break;
            }
            case NEW_MSG_UPDT_CODE: {
                // 4,$message_id,$flags,$from_id,$timestamp,$subject,$text,$attachments
                long mid = update.getLong(1);
                int mask = update.getInt(2);
                if ((mask & MSG_FLAG_VALUE_DELETED) == 0) {
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getGetMessageById(mid)));
                }
                break;
            }
            case MSG_DELETE_UPDT_CODE: {
                long mid = update.getLong(1);
                MessageStorage.getInstance().deleteMessage(mid);
                break;
            }
            case MSG_FLAGS_CHANGED_UPDT_CODE:
            case MSG_FLAG_ADDED_UPDT_CODE:
            case MSG_FLAG_REMOVED_UPDT_CODE: {
                long mid = update.getLong(1);
                int mask = update.getInt(2);
                Message msg = MessageStorage.getInstance().getMessageById(mid);
                if (msg != null) {
                    if ((mask & MSG_FLAG_VALUE_UNREAD) != 0) {
                        // сообщение прочитано если убирается флаг "unread"
                        // сообщение не прочитано если взводится влаг "unread" либо в новом наборе флагов есть "unread"
                        msg.setRead(updateCode == MSG_FLAG_REMOVED_UPDT_CODE);
                        msg.notifyChanges();
                        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_MESSAGES);
                        System.out.println("set msg read: " + msg.getText());
                    }

                    // обработка флага удаления:

                    if ((mask & MSG_FLAG_VALUE_DELETED) != 0) {
                        if (updateCode == MSG_FLAG_ADDED_UPDT_CODE || updateCode == MSG_FLAGS_CHANGED_UPDT_CODE) {
                            MessageStorage.getInstance().deleteMessage(mid);
                            System.out.println("set msg deleted: " + msg.getText());
                        } else {
                            BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getGetMessageById(mid)));
                        }
                    }
                } else {
                    if ((mask & MSG_FLAG_VALUE_DELETED) == 0) {// сообщение не удалено
                        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getGetMessageById(mid)));
                    }
                }
                break;
            }
            case USER_TYPING_UPDT_CODE: {
                long uid = update.getLong(1);
                MessageStorage.getInstance().markUserTyping(uid);
                break;
            }
            default:
                break;
            }
        } catch (Throwable e) {
            System.out.println("error in parsing update [" + update + "]");
            e.printStackTrace();
        }
    }

    private void setNewParams(LongPollServerParams params) {
        if (this.params == null) {
            this.params = params;
        } else {
            synchronized (this.params) {
                this.params = params;
            }
            synchronized (this.params) {
                this.params.notifyAll();
            }
        }
    }

    private class GetParamsTask extends BackgroundTask<LongPollServerParams> {
        private GetParamsTask() {
            super(1);
        }

        @Override
        public LongPollServerParams execute() throws Throwable {
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.getLongPollServer", null);
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
