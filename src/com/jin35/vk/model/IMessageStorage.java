package com.jin35.vk.model;

import java.util.Date;
import java.util.List;

public interface IMessageStorage {

    public abstract List<Message> getLastMessages();

    public abstract void addMessage(Message msg);

    public abstract void addMessages(List<Message> msgs);

    public abstract List<Message> getMessagesWithUser(long uid);

    public abstract void setSelection(Message msg, boolean selected);

    public abstract List<Message> getSelected();

    public abstract List<Message> clearSelected();

    public abstract boolean isSelected(Message msg);

    public abstract boolean hasUnreadMessagesFromUser(long uid);

    public abstract Message getMessageById(long mid);

    public abstract void deleteMessage(long mid);

    public abstract int getUreadMessageCount();

    public abstract void messageSent(long uid, String text, Long tmpMid, Date confirmedDate, long confirmedMid, boolean read);

    public abstract void dump();

}