package com.jin35.vk.adapters;

import android.view.View;
import android.view.View.OnClickListener;

import com.jin35.vk.model.IObjectListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public abstract class ConversationListItem extends ModelObjectListItem<Message> {

    public ConversationListItem(Message object) {
        super(object);
    }

    @Override
    public boolean needListener() {
        return true;
    }

    @Override
    public void subsribeListenerForObject(IObjectListener listener) {
        NotificationCenter.getInstance().addObjectListener(getObject().getId(), listener);
    }

    @Override
    public void updateView(View view) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = !v.isSelected();
                v.setSelected(selected);
                MessageStorage.getInstance().setSelection(getObject(), selected);
            }
        });
        System.out.println("is selected: " + MessageStorage.getInstance().isSelected(getObject()));
        view.setSelected(MessageStorage.getInstance().isSelected(getObject()));
        view.setClickable(true);
    }
}
