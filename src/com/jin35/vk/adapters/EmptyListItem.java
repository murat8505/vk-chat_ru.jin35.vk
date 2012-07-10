package com.jin35.vk.adapters;

import android.view.View;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.model.IModelListener;

public class EmptyListItem implements IListItem {

    private final int strResId;

    public EmptyListItem(int strResId) {
        this.strResId = strResId;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public int getViewId() {
        return R.layout.empty_list_item;
    }

    @Override
    public void updateView(View view) {
        ((TextView) view.findViewById(R.id.text_tv)).setText(strResId);
    }

    @Override
    public boolean needListener() {
        return false;
    }

    @Override
    public void subsribeListenerForObject(IModelListener listener) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
