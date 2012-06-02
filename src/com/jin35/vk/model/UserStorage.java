package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStorage {

    private static UserStorage instance;
    private Map<Long, UserInfo> users = new ConcurrentHashMap<Long, UserInfo>();

    private UserStorage() {
    }

    public synchronized static UserStorage getInstance() {
        if (instance == null)
            instance = new UserStorage();
        return instance;
    }

    public synchronized List<UserInfo> getAllUsers() {
        return new ArrayList<UserInfo>(users.values());
    }

    /**
     * call <b>notifyChanges</b> if there is some changes
     * */
    public synchronized UserInfo getUser(long userId) {
        return users.get(userId);
    }

    public synchronized void addUser(UserInfo user) throws IllegalArgumentException {
        users.remove(user.getId());
        users.put(user.getId(), user);
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.FRIENDS);
    }

    public synchronized void addUsers(List<UserInfo> newUsers) {
        for (UserInfo user : newUsers) {
            users.remove(user.getId());
            users.put(user.getId(), user);
        }
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.FRIENDS);
    }

    public UserInfo createUser(long userId) {
        return new UserInfo(userId);
    }
}
