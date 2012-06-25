package com.jin35.vk.model;

import java.util.List;

public interface IUserStorage {

    // UserInfo getUser(long id);
    UserInfo getUser(long id, boolean returnNullIfNoUser);

    List<UserInfo> getFriends();

    List<UserInfo> getOnlineFriends();

    List<UserInfo> getReguests();

    List<UserInfo> getSuggestions();

    void putNewUser(UserInfo user);

    void putNewUser(List<UserInfo> user);

    void markAsFriend(long id);

    void markAsFriend(List<Long> ids);

    void markAsFriends(List<UserInfo> ids);

    void markAsRequest(List<Long> ids);

    void markAsSuggestion(List<Long> ids);

    int getRequestsCount();

    void dump();
}
