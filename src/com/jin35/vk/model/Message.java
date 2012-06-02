package com.jin35.vk.model;

public class Message extends ModelObject {

    private long correspondentId;
    private boolean read;

    public Message(long id) {
        super(id);
    }

    @Override
    protected int getMaskForNotify() {
        return NotificationCenter.MESSAGE;
    }

}
