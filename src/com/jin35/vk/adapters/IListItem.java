package com.jin35.vk.adapters;

import android.view.View;

import com.jin35.vk.model.IObjectListener;

public interface IListItem {

    long getId();

    // View inflateView(LayoutInflater inflater, ViewGroup parent);
    int getViewId();

    void updateView(View view);

    boolean needListener();

    void subsribeListenerForObject(IObjectListener listener);

    boolean isEnabled();

}
