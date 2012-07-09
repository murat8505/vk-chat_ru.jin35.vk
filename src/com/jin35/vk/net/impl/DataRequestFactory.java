package com.jin35.vk.net.impl;

import java.util.List;

import android.graphics.Bitmap;

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

    public IDataRequest getDialogsRequest(int limit, int offset) {
        return new DialogsRequest(limit, offset);
    }

    public IDataRequest getMessagesWithUserRequest(long uid, int limit, int offset) {
        return new MessagesWithUserRequest(uid, limit, offset);
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

    public IDataRequest getSendMessageRequest(Message msg, List<Bitmap> attaches) {
        return new SendMessageRequest(msg, attaches);
    }

    public IDataRequest getMarkAsReadRequest(String unreadMids) {
        return new MarkAsReadRequest(unreadMids);
    }

    public IDataRequest getDeleteMessagesRequest(String mids) {
        return new DeleteMessagesRequest(mids);
    }

    public IDataRequest getMarkAsOnline() {
        return new MarkAsOnlineRequest();
    }

    public IDataRequest getDeleteUserRequest(long uid) {
        return new DeleteUserRequest(uid);
    }

    public IDataRequest getAddUserRequest(long uid) {
        return new AddUserRequest(uid);
    }

    public IDataRequest getCurrentUserPhotoRequest() {
        return new CurrentUserPhotoRequest();
    }

    public IDataRequest getGetMessageById(long mid) {
        return new MessageById(mid);
    }

    public IDataRequest getFullChatInfo(long chatId) {
        return new FullChatInfoRequest(chatId);
    }
}
