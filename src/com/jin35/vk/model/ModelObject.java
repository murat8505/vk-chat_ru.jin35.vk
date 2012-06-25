package com.jin35.vk.model;

public abstract class ModelObject {

    protected long id;

    protected ModelObject(long id) {
        this.id = id;
    }

    protected ModelObject() {
    }

    public void notifyChanges() {
        NotificationCenter.getInstance().notifyObjectListeners(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
