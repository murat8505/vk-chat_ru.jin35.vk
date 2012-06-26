package com.jin35.vk.adapters;

import android.view.View;

import com.jin35.vk.model.IModelListener;

public interface IListItem {

    long getId();

    int getViewId();

    void updateView(View view);

    boolean needListener();

    void subsribeListenerForObject(IModelListener listener);

    boolean isEnabled();

}
