package com.jin35.vk;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jin35.vk.model.IObjectListener;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorage;

public class AllFriendsAdapter extends Adapter<UserInfo> {
    public AllFriendsAdapter(Activity a) {
        super(a);
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_FRIENDS;
    }

    @Override
    protected List<UserInfo> getList() {
        List<UserInfo> result = UserStorage.getInstance().getFriends();
        return result;
    }

    @Override
    protected View getView(UserInfo object, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.friend_list_item, parent, false);
        }
        updateView(object, convertView);
        return convertView;
    }

    @Override
    protected void updateView(UserInfo object, View view) {
        if (object.getPhoto() != null) {
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(object.getPhoto());
        }
        int onlineVisibility = object.isOnline() ? View.VISIBLE : View.GONE;
        view.findViewById(R.id.online_indicator_iv).setVisibility(onlineVisibility);

        ((TextView) view.findViewById(R.id.name_tv)).setText(object.getFullName());
    }

    @Override
    protected void subsribeListenerForObject(IObjectListener newListener, UserInfo object) {
        NotificationCenter.getInstance().addObjectListener(object.getId(), newListener);
    }
}
