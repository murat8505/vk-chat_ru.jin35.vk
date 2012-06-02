package com.jin35.vk.net.impl;

import java.io.IOException;
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

class FriendsRequest implements IDataRequest {

    private static final String uidField = "uid";
    private static final String nameField = "first_name";
    private static final String familyField = "last_name";
    private static final String onlineField = "online";
    private static final String photoField = (PhoneInfo.useBigPhoto() ? "photo_medium_rec" : "photo_rec");

    FriendsRequest() {
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();

        try {
            params.put("order", "hints");
            params.put("count", "5");
            params.put("fields",
                    uidField.concat(",").concat(nameField).concat(",").concat(familyField).concat(",").concat(onlineField).concat(",").concat(photoField));
            JSONObject answer = VKRequestFactory.getInstance().getRequest().sendRequest("friends.get", params);
            UserStorage.getInstance().addUsers(makeUsers(answer, true));
        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        } catch (JSONException e) {
        }

        params.clear();
        try {
            params.put("fields",
                    uidField.concat(",").concat(nameField).concat(",").concat(familyField).concat(",").concat(onlineField).concat(",").concat(photoField));
            JSONObject answer = VKRequestFactory.getInstance().getRequest().sendRequest("friends.get", params);
            UserStorage.getInstance().addUsers(makeUsers(answer, false));
        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        } catch (JSONException e) {
        }
    }

    private List<UserInfo> makeUsers(JSONObject serverAnswer, boolean useImportance) throws JSONException {
        List<UserInfo> result = new ArrayList<UserInfo>();
        if (serverAnswer.has("response")) {
            JSONArray array = serverAnswer.getJSONArray("response");
            for (int i = 0; i < array.length(); i++) {
                JSONObject JSONUserInfo = (JSONObject) array.get(i);
                long id = JSONUserInfo.getLong(uidField);
                UserInfo user = UserStorage.getInstance().createUser(id);
                user.setFamilyName(JSONUserInfo.getString(familyField));
                user.setName(JSONUserInfo.getString(nameField));
                user.setOnline(JSONUserInfo.getInt(onlineField) == 1);
                user.setPhotoUrl(JSONUserInfo.getString(photoField));
                if (useImportance) {
                    user.setImportance(100 - i);
                }
                result.add(user);
            }
        }
        return result;
    }
}
