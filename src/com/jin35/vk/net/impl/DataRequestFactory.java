package com.jin35.vk.net.impl;

import java.util.List;

import com.jin35.vk.model.Message;
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

    public IDataRequest getDialogsRequest() {
        return new DialogsRequest();
    }

    public IDataRequest getMessagesWithUserRequest(long uid) {
        return new MessagesWithUserRequest(uid);
    }

    public IDataRequest getRequestesRequest() {
        return new RequestsRequest();
    }

    public IDataRequest getSuggestionsRequest() {
        return new SuggestionsRequest();
    }

    public IDataRequest getSendMessageRequest(Message msg) {
        return new SendMessageRequest(msg);
    }

    public IDataRequest getMarkAsReadRequest(String unreadMids) {
        return new MarkAsReadRequest(unreadMids);
    }
}
