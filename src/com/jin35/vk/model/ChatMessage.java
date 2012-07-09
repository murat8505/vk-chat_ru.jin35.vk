package com.jin35.vk.model;

import java.util.Date;

public class ChatMessage extends Message {

    private final long authorId;

    public ChatMessage(long id, long chatId, String text, Date time, boolean income, long authorId) {
        super(id, chatId, text, time, income);
        this.authorId = authorId;
    }

    public long getAuthorId() {
        return authorId;
    }

}
