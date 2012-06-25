package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;

public class NotificationCenter {
    public static final int MODEL_FRIENDS = 2;
    public static final int MODEL_MESSAGES = 4;
    public static final int MODEL_REQUESTS = 8;
    public static final int MODEL_ONLINE = 32;
    public static final int MODEL_SUGGESTIONS = 64;
    public static final int MODEL_SELECTED = 128;

    private static NotificationCenter instance;

    private final Handler handler;

    private NotificationCenter(Handler handler) {
        this.handler = handler;
    }

    public static void init(Handler handler) {
        instance = new NotificationCenter(handler);
    }

    public static NotificationCenter getInstance() {
        return instance;
    }

    private final Map<IObjectListener, List<Long>> objectListeners = new ConcurrentHashMap<IObjectListener, List<Long>>();
    private final Map<IModelListener, Integer> modelListeners = new ConcurrentHashMap<IModelListener, Integer>();

    public synchronized void addObjectListener(final long id, IObjectListener listener) {
        List<Long> idList = objectListeners.get(listener);
        if (idList == null) {
            idList = new ArrayList<Long>();
            objectListeners.put(listener, idList);
        }
        if (!idList.contains(id)) {
            idList.add(id);
        }
    }

    public void addModelListener(int mask, IModelListener listener) {
        Integer currentValue = modelListeners.remove(listener);
        if (currentValue == null) {
            currentValue = 0;
        }
        modelListeners.put(listener, mask | currentValue);
    }

    public void removeListener(IModelListener listener) {
        modelListeners.remove(listener);
    }

    public void removeListener(IObjectListener listener) {
        objectListeners.remove(listener);
    }

    void notifyObjectListeners(final long id) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (IObjectListener listener : objectListeners.keySet()) {
                    List<Long> value = objectListeners.get(listener);
                    if (value.contains(id)) {
                        try {
                            listener.dataChanged(id);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }

    public void notifyModelListeners(final int mask) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (IModelListener listener : modelListeners.keySet()) {
                    Integer value = modelListeners.get(listener);
                    if ((value & mask) != 0) {
                        try {
                            listener.dataChanged();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
