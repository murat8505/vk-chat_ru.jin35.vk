package com.jin35.vk.net.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorage;

class FriendsRequest extends BaseUsersRequest {

    private boolean userImportance = false;

    FriendsRequest() {
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();

        params.put("order", "hints");
        params.put("count", "5");
        userImportance = true;
        execute(params);
        userImportance = false;
        execute(null);
    }

    @Override
    protected String getMethodName() {
        return "friends.get";
    }

    @Override
    protected void onUserCreated(UserInfo user, int userOrder) {
        if (userImportance) {
            user.setImportance(5 - userOrder);
        }
    }

    @Override
    protected void onResult(List<UserInfo> users) {
        UserStorage.getInstance().addFriends(users);
    }
}
