package com.jin35.vk.model;

import java.util.Date;

public class Message extends ModelObject {

    private final long correspondentId;
    private boolean read;
    private final String text;
    private final Date time;

    public Message(long id, long correspondentId, String text, Date time) {
        super(id);
        this.correspondentId = correspondentId;
        this.text = text;
        this.time = time;
    }

    public long getCorrespondentId() {
        return correspondentId;
    }

    public String getText() {
        return text;
    }

    public Date getTime() {
        return time;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
