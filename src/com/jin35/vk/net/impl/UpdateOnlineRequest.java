package com.jin35.vk.net.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;

public class UpdateOnlineRequest implements IDataRequest {

    @Override
    public void execute() {
        try {
            List<Long> uids = UserStorageFactory.getInstance().getUserStorage().getAllUsers();

            List<String> strUids = new ArrayList<String>();
            int packSize = 0;
            String nextPack = "";
            for (int i = 0; i < uids.size(); i++) {
                packSize++;
                nextPack += uids.get(i) + ",";
                if (packSize == 1000) {
                    strUids.add(nextPack.substring(0, nextPack.length() - 1));
                    packSize = 0;
                    nextPack = "";
                }
            }
            if (nextPack.length() > 0) {
                strUids.add(nextPack.substring(0, nextPack.length() - 1));
            }

            for (String uidsParam : strUids) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uids", uidsParam);
                params.put("fields", "online");
                JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("users.get", params);
                if (answer.has(responseParam)) {
                    JSONArray array = answer.getJSONArray(responseParam);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject oneUid = array.getJSONObject(i);
                        UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(oneUid.getLong("uid"), true);
                        if (user != null) {
                            boolean oldOnline = user.isOnline();
                            boolean newOnline = oneUid.getInt("online") == 1;
                            if (oldOnline != newOnline) {
                                user.setOnline(newOnline);
                                user.notifyChanges();
                            }
                        }

                    }

                }
            }
        } catch (Exception e) {
        }
    }
}
