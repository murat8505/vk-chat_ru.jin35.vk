package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        if (instance == null) {
            instance = new NotificationCenter(handler);
        }
    }

    public static NotificationCenter getInstance() {
        return instance;
    }

    private final Map<IModelListener, List<Long>> objectListeners = new ConcurrentHashMap<IModelListener, List<Long>>();
    private final Map<IModelListener, Integer> modelListeners = new ConcurrentHashMap<IModelListener, Integer>();
    private final Map<IModelListener, List<Long>> conversationListeners = new ConcurrentHashMap<IModelListener, List<Long>>();

    public synchronized void addObjectListener(final long id, IModelListener listener) {
        addListenerWithId(id, listener, objectListeners);
    }

    public synchronized void addConversationListener(final long id, IModelListener listener) {
        addListenerWithId(id, listener, conversationListeners);
    }

    private void addListenerWithId(long id, IModelListener listener, Map<IModelListener, List<Long>> listeners) {
        List<Long> idList = listeners.get(listener);
        if (idList == null) {
            idList = new ArrayList<Long>();
            listeners.put(listener, idList);
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
        objectListeners.remove(listener);
        modelListeners.remove(listener);
        conversationListeners.remove(listener);
    }

    public void notifyObjectListeners(final long id) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (IModelListener listener : objectListeners.keySet()) {
                    List<Long> value = objectListeners.get(listener);
                    if (value.contains(id)) {
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

    public void notifyConversationListeners(final List<Long> ids) {
        System.out.println("notify conversations with user: " + Arrays.toString(ids.toArray()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (IModelListener listener : conversationListeners.keySet()) {
                    List<Long> value = conversationListeners.get(listener);
                    if (!Collections.disjoint(value, ids)) {
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
