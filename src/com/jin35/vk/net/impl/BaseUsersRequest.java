package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jin35.vk.PhoneInfo;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;

abstract class BaseUsersRequest implements IDataRequest {

    private static final String uidField = "uid";
    private static final String nameField = "first_name";
    private static final String familyField = "last_name";
    private static final String onlineField = "online";
    private static final String photoField = (PhoneInfo.useBigPhoto() ? "photo_medium_rec" : "photo_rec");

    protected BaseUsersRequest() {
    }

    protected abstract String getMethodName();

    protected abstract void onResult(List<UserInfo> users);

    protected final void execute(Map<String, String> specialParams) {
        if (specialParams == null) {
            specialParams = new HashMap<String, String>();
        }
        specialParams.put("fields", getFileds());
        try {
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer(getMethodName(), specialParams);
            onResult(makeUsers(answer));
        } catch (Throwable e) {
        }
    }

    static String getFileds() {
        return uidField.concat(",").concat(nameField).concat(",").concat(familyField).concat(",").concat(onlineField).concat(",").concat(photoField);
    }

    private List<UserInfo> makeUsers(JSONObject serverAnswer) throws JSONException {
        List<UserInfo> result = new ArrayList<UserInfo>();
        if (serverAnswer.has(responseParam)) {
            JSONArray array = serverAnswer.getJSONArray(responseParam);
            for (int i = 0; i < array.length(); i++) {
                JSONObject JSONUserInfo = (JSONObject) array.get(i);
                UserInfo user = getUser(JSONUserInfo);
                onUserCreated(user, i);
                result.add(user);
            }
        }
        return result;
    }

    static UserInfo getUser(JSONObject data) throws JSONException {
        long id = data.getLong(uidField);
        UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(id, false);
        user.setFamilyName(data.getString(familyField));
        user.setName(data.getString(nameField));
        user.setOnline(data.getInt(onlineField) == 1);
        user.setPhotoUrl(data.getString(photoField));
        return user;
    }

    protected void onUserCreated(UserInfo user, int userOrder) {
    }
}
