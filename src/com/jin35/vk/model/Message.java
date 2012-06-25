package com.jin35.vk.model;

import java.util.Comparator;
import java.util.Date;

public class Message extends ModelObject {

    private final long correspondentId;
    private boolean read;
    private final boolean income;
    private boolean sent = true;
    private final String text;
    private Date time;
    private String unique;

    public Message(long id, long correspondentId, String text, Date time, boolean income) {
        super(id);
        this.correspondentId = correspondentId;
        this.text = text;
        this.time = time;
        this.income = income;
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

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isIncome() {
        return income;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isSent() {
        return sent;
    }

    public static Comparator<Message> getDescendingTimeComparator() {
        return new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return lhs.getTime().compareTo(rhs.getTime());
            }
        };
    }

    public static Comparator<Message> getAscendingTimeComparator() {
        return new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return rhs.getTime().compareTo(lhs.getTime());
            }
        };
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public String getUnique() {
        return unique;
    }

    private static volatile long tempId = Long.MAX_VALUE;

    public static long getUniqueTempId() {
        return --tempId;
    }

    @Override
    public String toString() {
        return "Message from [" + correspondentId + "]. \"" + text + "\"";
    }
}
