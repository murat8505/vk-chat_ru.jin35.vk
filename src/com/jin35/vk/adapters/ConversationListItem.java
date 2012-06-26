package com.jin35.vk.adapters;

import android.view.View;
import android.view.View.OnClickListener;

import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;

public abstract class ConversationListItem extends ModelObjectListItem<Message> {
    private final Adapter<?> adapter;

    public ConversationListItem(Adapter<?> adapter, Message object) {
        super(object);
        this.adapter = adapter;
    }

    @Override
    public boolean needListener() {
        return true;
    }

    @Override
    public void subsribeListenerForObject(IModelListener listener) {
        NotificationCenter.getInstance().addObjectListener(getObject().getId(), listener);
    }

    @Override
    public void updateView(final View view) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = !MessageStorage.getInstance().isSelected(getObject());
                view.setSelected(selected);
                MessageStorage.getInstance().setSelection(getObject(), selected);
            }
        });
        // TODO далее идет суровый костыль,
        // но если напрямую вызывать view.setSelected
        // то отрисовка не отработает
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setSelected(MessageStorage.getInstance().isSelected(getObject()));
            }
        }, 1);
    }

}
