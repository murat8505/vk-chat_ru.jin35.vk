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

    synchronized static IUserStorage getInstance() {
        if (instance == null) {
            instance = new UserStorage();
        }
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
        System.out.println("get friends: " + friends.size());
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
    }

    @Override
    public void markAsFriend(List<Long> ids) {
        if (addToList(ids, friends)) {
            notifyModelFriends();
        }
    }

    @Override
    public void markAsFriends(List<UserInfo> users) {
        List<Long> ids = new ArrayList<Long>();
        for (UserInfo user : users) {
            ids.add(user.getId());
        }
        markAsFriend(ids);
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
        System.out.println("notifyModelRequests");
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

}
