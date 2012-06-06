package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStorage {

    private static UserStorage instance;
    private final Map<Long, UserInfo> users = new ConcurrentHashMap<Long, UserInfo>();
    private final Map<Long, UserInfo> friends = new ConcurrentHashMap<Long, UserInfo>();
    private final Map<Long, UserInfo> requests = new ConcurrentHashMap<Long, UserInfo>();

    private UserStorage() {
    }

    public synchronized static UserStorage getInstance() {
        if (instance == null) {
            instance = new UserStorage();
        }
        return instance;
    }

    public synchronized List<UserInfo> getFriends() {
        return new ArrayList<UserInfo>(friends.values());
    }

    public synchronized List<UserInfo> getOnlineFriends() {
        List<UserInfo> result = new ArrayList<UserInfo>();
        for (UserInfo user : friends.values()) {
            if (user.isOnline()) {
                result.add(user);
            }
        }
        return result;
    }

    public synchronized List<UserInfo> getRequests() {
        return new ArrayList<UserInfo>(requests.values());
    }

    /**
     * call <b>notifyChanges</b> if there is some changes
     * */
    public synchronized UserInfo getUser(long userId) {
        UserInfo result = null;
        result = friends.get(userId);
        if (result != null) {
            return result;
        }
        result = requests.get(userId);
        if (result != null) {
            return result;
        }
        return users.get(userId);
    }

    public synchronized void addFriends(List<UserInfo> friends) {
        for (UserInfo friend : friends) {
            addFriend(friend, false);
        }
        notifyModelFriends();
    }

    private synchronized void addFriend(UserInfo friend, boolean notify) {
        add(friends, friend);
        if (notify) {
            notifyModelFriends();
        }
    }

    public synchronized void addRequests(List<UserInfo> requests) {
        for (UserInfo request : requests) {
            addRequest(request, false);
        }
        notifyModelRequests();
    }

    private synchronized void addRequest(UserInfo request, boolean notify) {
        add(requests, request);
        if (notify) {
            notifyModelRequests();
        }
    }

    public synchronized void addUsers(List<UserInfo> users) {
        for (UserInfo user : users) {
            addUser(user, false);
        }
        notifyModelUsers();
    }

    private synchronized void addUser(UserInfo user, boolean notify) {
        add(users, user);
        if (notify) {
            notifyModelUsers();
        }
    }

    public synchronized void addFriendFromRequest() {
    }

    public synchronized void removeFriend() {
    }

    public synchronized void removeRequest() {
    }

    public synchronized void removeUser() {
    }

    private synchronized void add(Map<Long, UserInfo> map, UserInfo user) {
        long uid = user.getId();
        map.remove(uid);
        map.put(uid, user);// TODO merge?
    }

    private void notifyModelFriends() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_FRIENDS);
    }

    private void notifyModelRequests() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_REQUESTS);
    }

    private void notifyModelUsers() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_USERS);
    }
}
