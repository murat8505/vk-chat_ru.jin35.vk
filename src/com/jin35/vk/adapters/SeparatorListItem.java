package com.jin35.vk.adapters;

import android.view.View;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.model.IModelListener;

public class SeparatorListItem implements IListItem {
    private final String text;

    public SeparatorListItem(String text) {
        this.text = text;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public int getViewId() {
        return R.layout.separator_list_item;
    }

    @Override
    public void updateView(View view) {
        ((TextView) view).setText(text);
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
