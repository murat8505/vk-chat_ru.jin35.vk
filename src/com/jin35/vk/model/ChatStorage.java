package com.jin35.vk.model;

import java.util.HashMap;
import java.util.Map;

public class ChatStorage {

    private static ChatStorage instance;

    private final Map<Long, Chat> chats = new HashMap<Long, Chat>();

    private ChatStorage() {
    }

    public static ChatStorage getInstance() {
        if (instance == null) {
            instance = new ChatStorage();
        }
        return instance;
    }

    public Chat getChat(long id) {
        return chats.get(id);
    }

    public void addChat(Chat chat) {
        chats.remove(chat.getId());
        chats.put(chat.getId(), chat);
    }
}