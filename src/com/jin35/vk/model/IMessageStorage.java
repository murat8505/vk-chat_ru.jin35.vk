package com.jin35.vk.model;

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

    public abstract void deleteMessage(List<Message> msgs);

    public abstract void deleteMessage(long mid);

    public abstract int getUreadMessageCount();

    // public abstract void messageSent(long uid, String text, Long tmpMid, Date confirmedDate, long confirmedMid, boolean read);

    public abstract void dump();

    public abstract void markUserTyping(Long uid);

    public abstract boolean isUserTyping(Long uid);

    public abstract boolean hasMoreDialogs();

    public abstract boolean hasMoreMessagesWithUser(Long uid);

    int getDownloadedDialogCount();

    int getDownloadedMessageCount(Long uid);

    void setMessagesWithUserCount(Long uid, int count);

    void setDownloadedDialogCount(int count);

    List<ChatMessage> getMessagesFromChat(long chatId);

    boolean hasUnreadMessagesFromChat(long chatId);

    void markChatUserTyping(Long chatId, Long uid);

    List<Long> getUsersTyping(Long chatId);

    boolean hasMoreChatMessages(Long chatId);

    void setChatMessagesCount(Long chatId, int count);

    int getDownloadedChatMessageCount(Long chatId);

    void updateMsgId(Message msg, long newMid);
}