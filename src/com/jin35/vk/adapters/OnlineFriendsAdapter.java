package com.jin35.vk.adapters;

import java.util.List;

import com.jin35.vk.FriendsActivity;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class OnlineFriendsAdapter extends FriendsAdapter {

    public OnlineFriendsAdapter(FriendsActivity a) {
        super(a);
    }

    @Override
    protected List<UserInfo> getUsers() {
        return UserStorageFactory.getInstance().getUserStorage().getOnlineFriends();
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_FRIENDS & NotificationCenter.MODEL_ONLINE;
    }

}
