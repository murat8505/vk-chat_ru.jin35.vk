package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationCenter {
    public static final int MODEL_FRIENDS = 2;
    public static final int MODEL_MESSAGES = 4;
    public static final int MODEL_REQUESTS = 8;
    public static final int MODEL_USERS = 16;

    private static NotificationCenter instance;

    private NotificationCenter() {
    }

    public static NotificationCenter getInstance() {
        if (instance == null) {
            instance = new NotificationCenter();
        }
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

    void notifyObjectListeners(long id) {
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

    public void notifyModelListeners(int mask) {
        for (IModelListener listener : modelListeners.keySet()) {
            Integer value = modelListeners.get(listener);
            if ((value & mask) != 0) {
                try {
                    listener.dataChanged();
                } catch (Throwable e) {
                }
            }
        }
    }
}
