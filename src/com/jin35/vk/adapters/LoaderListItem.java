package com.jin35.vk.adapters;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.jin35.vk.R;
import com.jin35.vk.model.IModelListener;

public class LoaderListItem implements IListItem {

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public int getViewId() {
        return R.layout.downloading_list_item;
    }

    @Override
    public void updateView(View view) {
        final AnimationDrawable loader = (AnimationDrawable) ((ImageView) view.findViewById(R.id.loader_iv)).getDrawable();
        view.post(new Runnable() {
            @Override
            public void run() {
                loader.start();
            }
        });
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
