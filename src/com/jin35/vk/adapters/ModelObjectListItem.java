package com.jin35.vk.adapters;

import com.jin35.vk.model.ModelObject;

public abstract class ModelObjectListItem<T extends ModelObject> implements IListItem {

    private final T object;

    public ModelObjectListItem(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    @Override
    public long getId() {
        return object.getId();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
