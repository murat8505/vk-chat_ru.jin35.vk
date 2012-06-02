package com.jin35.vk.net.impl;

import com.jin35.vk.net.IDataRequest;

public class DataRequestFactory {

    private static DataRequestFactory instance;

    private DataRequestFactory() {
    }

    public static DataRequestFactory getInstance() {
        if (instance == null)
            instance = new DataRequestFactory();
        return instance;
    }

    public IDataRequest getFriendsRequest() {
        return new FriendsRequest();
    }
}
