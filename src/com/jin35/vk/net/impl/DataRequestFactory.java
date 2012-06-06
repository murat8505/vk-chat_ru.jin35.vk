package com.jin35.vk.net.impl;

import java.util.List;

import com.jin35.vk.net.IDataRequest;

public class DataRequestFactory {

    private static DataRequestFactory instance;

    private DataRequestFactory() {
    }

    public static DataRequestFactory getInstance() {
        if (instance == null) {
            instance = new DataRequestFactory();
        }
        return instance;
    }

    public IDataRequest getFriendsRequest() {
        return new FriendsRequest();
    }

    public IDataRequest getUsersRequest(List<Long> uids) {
        return new UsersRequest(uids);
    }

    public IDataRequest getMessagesRequest() {
        return new MessagesRequest();
    }

    public IDataRequest getLongPollServerParamsRequest() {
        return null;
    }
}
