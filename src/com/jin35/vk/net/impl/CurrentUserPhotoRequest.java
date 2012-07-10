package com.jin35.vk.net.impl;

import java.util.Arrays;
import java.util.List;

import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.net.Token;

public class CurrentUserPhotoRequest extends UsersRequest {

    public CurrentUserPhotoRequest() {
        super(Arrays.asList(new Long[] { Token.getInstance().getCurrentUid() }));
    }

    @Override
    protected void onResult(List<UserInfo> users) {
        Token.getInstance().setCurrentUserPhoto(users.get(0).getPhotoUrl());
        PhotoStorage.getInstance().getPhoto(users.get(0));
    }

}
