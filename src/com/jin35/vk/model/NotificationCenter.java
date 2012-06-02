package com.jin35.vk.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Pair;

public class NotificationCenter {
    public static final int FRIENDS = 2;
    public static final int MESSAGE = 4;

    private static NotificationCenter instance;

    private NotificationCenter() {
    }

    public static NotificationCenter getInstance() {
        if (instance == null)
            instance = new NotificationCenter();
        return instance;
    }

    private Map<IObjectListener, Pair<Integer, Long>> objectListeners = new ConcurrentHashMap<IObjectListener, Pair<Integer, Long>>();
    private Map<IModelListener, Integer> modelListeners = new ConcurrentHashMap<IModelListener, Integer>();

    public void addObjectListener(int mask, long id, IObjectListener listener) {
        objectListeners.remove(listener);
        objectListeners.put(listener, new Pair<Integer, Long>(mask, id));
    }

    public void addModelListener(int mask, IModelListener listener) {
        modelListeners.remove(listener);
        modelListeners.put(listener, mask);
    }

    public void removeListener(IModelListener listener) {
        modelListeners.remove(listener);
    }

    public void removeListener(IObjectListener listener) {
        objectListeners.remove(listener);
    }

    protected void notifyObjectListeners(int mask, long id) {
        for (IObjectListener listener : objectListeners.keySet()) {
            Pair<Integer, Long> value = objectListeners.get(listener);
            if ((value.first & mask) != 0 && id == value.second)
                try {
                    listener.dataChanged(id);
                } catch (Throwable e) {
                }
        }
    }

    public void notifyModelListeners(int mask) {
        for (IModelListener listener : modelListeners.keySet()) {
            Integer value = modelListeners.get(listener);
            if ((value & mask) != 0)
                try {
                    listener.dataChanged();
                } catch (Throwable e) {
                }
        }
    }
}
