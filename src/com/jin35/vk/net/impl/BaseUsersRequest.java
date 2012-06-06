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
import com.jin35.vk.model.UserStorage;
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
        specialParams.put("fields",
                uidField.concat(",").concat(nameField).concat(",").concat(familyField).concat(",").concat(onlineField).concat(",").concat(photoField));
        try {
            System.out.println("users request, " + getMethodName());
            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequest(getMethodName(), specialParams);
            System.out.println("users request answer, " + getMethodName());
            onResult(makeUsers(answer));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private List<UserInfo> makeUsers(JSONObject serverAnswer) throws JSONException {
        List<UserInfo> result = new ArrayList<UserInfo>();
        if (serverAnswer.has("response")) {
            JSONArray array = serverAnswer.getJSONArray("response");
            for (int i = 0; i < array.length(); i++) {
                JSONObject JSONUserInfo = (JSONObject) array.get(i);
                long id = JSONUserInfo.getLong(uidField);
                UserInfo user = UserStorage.getInstance().getUser(id);
                if (user == null) {
                    user = new UserInfo(id);
                }
                user.setFamilyName(JSONUserInfo.getString(familyField));
                user.setName(JSONUserInfo.getString(nameField));
                user.setOnline(JSONUserInfo.getInt(onlineField) == 1);
                user.setPhotoUrl(JSONUserInfo.getString(photoField));
                onUserCreated(user, i);
                result.add(user);
            }
        }
        return result;
    }

    protected void onUserCreated(UserInfo user, int userOrder) {
    }
}
