package com.jin35.vk;

import java.util.List;

import android.app.Activity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jin35.vk.model.IObjectListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorage;

public class MessagesAdapter extends Adapter<Message> {
    public MessagesAdapter(Activity a) {
        super(a);
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_MESSAGES;
    }

    @Override
    protected List<Message> getList() {
        return MessageStorage.getInstance().getLastMessages();
    }

    @Override
    protected View getView(Message object, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.message_list_item, parent, false);
        }
        updateView(object, convertView);
        return convertView;
    }

    @Override
    protected void updateView(Message object, View view) {
        UserInfo correspondent = UserStorage.getInstance().getUser(object.getCorrespondentId());
        if (correspondent == null) {
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
            view.findViewById(R.id.online_indicator_iv).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.name_tv)).setText("...");
        } else {
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(correspondent.getPhoto());
            int onlineVisibility = correspondent.isOnline() ? View.VISIBLE : View.GONE;
            view.findViewById(R.id.online_indicator_iv).setVisibility(onlineVisibility);
            ((TextView) view.findViewById(R.id.name_tv)).setText(correspondent.getFullName());
        }
        ((TextView) view.findViewById(R.id.text_tv)).setText(object.getText());
        ((TextView) view.findViewById(R.id.time_tv)).setText(DateFormat.getTimeFormat(activity).format(object.getTime()));
    }

    @Override
    protected void subsribeListenerForObject(IObjectListener listener, Message object) {
        NotificationCenter.getInstance().addObjectListener(object.getId(), listener);
        NotificationCenter.getInstance().addObjectListener(object.getCorrespondentId(), listener);
    }
}
