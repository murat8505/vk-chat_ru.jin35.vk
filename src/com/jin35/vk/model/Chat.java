package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.List;

public class Chat extends ModelObject {

    private String chatName;
    private final List<Long> users = new ArrayList<Long>();
    private long admin;

    public Chat(long id) {
        super(id);
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public List<Long> getUsers() {
        return users;
    }

    public void addUser(Long uid) {
        if (!users.contains(uid)) {
            users.add(uid);
        }
    }

    public long getAdmin() {
        return admin;
    }

    public void setAdmin(long admin) {
        this.admin = admin;
    }
}
