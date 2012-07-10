package com.jin35.vk.model.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.UserInfo;

public interface IDB {

    Bitmap getPhoto(String photoUrl);

    void savePhoto(String photoUrl, Bitmap photo);

    void dumpUsersLists(Map<Long, UserInfo> users, List<Long> friends, List<Long> requests);

    void cacheUsers();

    void dumpMessages(Collection<Map<Long, Message>> messages, Collection<Map<Long, ChatMessage>> collection);

    void saveMessage(Message message);

    void cacheMessages();

    void clearCache();
}