package com.jin35.vk.model;

import java.util.Comparator;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class UserInfo extends ModelObject {

    private String name;
    private String familyName;
    private boolean online;
    private int importance;
    private String photoUrl;

    public UserInfo(long userId) {
        super(userId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Drawable getPhoto() {
        return PhotoStorage.getInstance().getPhoto(photoUrl, id);
    }

    public String getFullName() {
        String result = "";
        if (!TextUtils.isEmpty(name)) {
            result = result.concat(name);
        }
        if (!TextUtils.isEmpty(familyName)) {
            if (result.length() > 0) {
                result = result.concat(" ");
            }
            result = result.concat(familyName);
        }
        return result;
    }

    public static Comparator<UserInfo> getFriendComparator() {
        return new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo lhs, UserInfo rhs) {
                if (lhs.getImportance() != rhs.getImportance()) {
                    return rhs.getImportance() - lhs.getImportance();
                }
                return lhs.getFullName().compareTo(rhs.getFullName());
            }
        };
    }
}
