package com.jin35.vk.model;

import java.util.List;

public interface INotificationCenter {

    public static final int MODEL_FRIENDS = 2;
    public static final int MODEL_MESSAGES = 4;
    public static final int MODEL_REQUESTS = 8;
    public static final int MODEL_USERS = 16;
    public static final int MODEL_ONLINE = 32;

    void notifyFriendsListChanged();

    void notifyOnlineChanged(long id);

    void notifyOnlineChanged(List<Long> ids);

    void notifyMessagesListChanged();

    void notifyRequestsListChanged();

    void notifyPossibleFriendsListChanged();

}
