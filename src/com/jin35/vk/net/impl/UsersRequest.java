package com.jin35.vk.net.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorage;

public class UsersRequest extends BaseUsersRequest {

    private final List<Long> uids;

    public UsersRequest(List<Long> uids) {
        this.uids = uids;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();

        String uidsString = "";
        for (Long uid : uids) {
            uidsString = uidsString.concat(",").concat(uid.toString());
        }
        uidsString = uidsString.substring(1);
        params.put("uids", uidsString);
        execute(params);
    }

    @Override
    protected String getMethodName() {
        return "users.get";
    }

    @Override
    protected void onResult(List<UserInfo> users) {
        UserStorage.getInstance().addUsers(users);
    }
}
