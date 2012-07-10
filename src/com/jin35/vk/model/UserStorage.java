package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.impl.BackgroundTask;
import com.jin35.vk.net.impl.BackgroundTasksQueue;

class UserStorage implements IUserStorage {

    private static IUserStorage instance;
    private final Map<Long, UserInfo> users = new ConcurrentHashMap<Long, UserInfo>();
    private final List<Long> friends = new ArrayList<Long>();
    private final List<Long> requests = new ArrayList<Long>();
    private final List<Long> suggestions = new ArrayList<Long>();

    private UserStorage() {
    }

    synchronized static boolean init() {
        if (instance == null) {
            instance = new UserStorage();
            return true;
        }
        return false;
    }

    synchronized static IUserStorage getInstance() {
        return instance;
    }

    @Override
    public UserInfo getUser(long id, boolean returnNullIfNoUser) {
        UserInfo user = users.get(id);
        if (user == null && !returnNullIfNoUser) {
            user = new UserInfo(id);
            users.put(id, user);
        }
        return user;
    }

    @Override
    public List<UserInfo> getFriends() {
        return getUsersFromList(friends);
    }

    @Override
    public List<UserInfo> getOnlineFriends() {
        List<UserInfo> result = new ArrayList<UserInfo>();
        for (long id : friends) {
            UserInfo user = users.get(id);
            if (user.isOnline()) {
                result.add(user);
            }
        }
        return result;
    }

    @Override
    public List<UserInfo> getReguests() {
        return getUsersFromList(requests);
    }

    @Override
    public List<UserInfo> getSuggestions() {
        return getUsersFromList(suggestions);
    }

    @Override
    public void putNewUser(UserInfo user) {
        if (user != null) {
            users.remove(user.id);
            users.put(user.id, user);
        }
    }

    @Override
    public void putNewUser(List<UserInfo> users) {
        for (UserInfo user : users) {
            putNewUser(user);
        }
    }

    @Override
    public void markAsFriend(long id) {
        if (!friends.contains(id)) {
            friends.add(id);
            notifyModelFriends();
        }
        if (requests.contains(id)) {
            requests.remove(id);
            notifyModelRequests();
        }
    }

    @Override
    public void markAsFriend(List<Long> ids) {
        if (addToList(ids, friends)) {
            notifyModelFriends();
        }
    }

    @Override
    public synchronized void newFriendList(List<UserInfo> users) {
        friends.clear();
        for (UserInfo user : users) {
            friends.add(user.getId());
        }
        notifyModelFriends();
    }

    @Override
    public synchronized void newRequestList(List<Long> users) {
        requests.clear();
        for (Long uid : users) {
            requests.add(uid);
        }
        notifyModelRequests();
    }

    @Override
    public void markAsRequest(List<Long> ids) {
        if (addToList(ids, requests)) {
            notifyModelRequests();
        }
    }

    @Override
    public void markAsSuggestion(List<Long> ids) {
        if (addToList(ids, suggestions)) {
            notifyModelSuggestions();
        }
    }

    @Override
    public int getRequestsCount() {
        return requests.size();
    }

    private List<UserInfo> getUsersFromList(List<Long> list) {
        List<UserInfo> result = new ArrayList<UserInfo>();
        for (Long id : list) {
            result.add(users.get(id));
        }
        return result;
    }

    private boolean addToList(List<Long> source, List<Long> destination) {
        boolean result = false;
        for (long id : source) {
            if (!destination.contains(id)) {
                destination.add(id);
                result = true;
            }
        }
        return result;
    }

    private void notifyModelFriends() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_FRIENDS);
    }

    private void notifyModelRequests() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_REQUESTS);
    }

    private void notifyModelSuggestions() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_SUGGESTIONS);
    }

    @Override
    public void dump() {
        BackgroundTasksQueue.getInstance().execute(new BackgroundTask<Void>(10) {

            @Override
            public Void execute() throws Throwable {
                DB.getInstance().dumpUsersLists(users, friends, requests);
                return null;
            }

            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError() {
            }
        });
    }

    @Override
    public boolean isFriend(long uid) {
        return friends.contains(uid);
    }

    @Override
    public boolean isRequest(long uid) {
        return requests.contains(uid);
    }

    @Override
    public void removeFriend(long uid) {
        friends.remove(uid);
        notifyModelFriends();
    }

    @Override
    public void removeRequest(long uid) {
        requests.remove(uid);
        notifyModelRequests();
    }

    @Override
    public List<Long> getAllUsers() {
        return new ArrayList<Long>(users.keySet());
    }

}
