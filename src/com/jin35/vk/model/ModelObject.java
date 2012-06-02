package com.jin35.vk.model;

public abstract class ModelObject {

    protected long id;

    protected abstract int getMaskForNotify();

    protected ModelObject(long id) {
        this.id = id;
    }

    public void notifyChanges() {
        NotificationCenter.getInstance().notifyObjectListeners(getMaskForNotify(), id);
    }

    public long getId() {
        return id;
    }
}
