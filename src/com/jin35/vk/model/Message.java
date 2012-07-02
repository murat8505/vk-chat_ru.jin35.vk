package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.util.Pair;

public class Message extends ModelObject {

    private final long correspondentId;
    private boolean read;
    private final boolean income;
    private boolean sent = true;
    private final String text;
    private Date time;
    private String unique;
    private boolean deleting = false;

    private Pair<Double, Double> location;
    private List<Long> forwarded = new ArrayList<Long>();
    private AttachmentPack attachmentPack;

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

    public void setLocation(Pair<Double, Double> location) {
        this.location = location;
    }

    public Pair<Double, Double> getLocation() {
        return location;
    }

    public List<Long> getForwarded() {
        return forwarded;
    }

    public void setForwarded(List<Long> forwarded) {
        this.forwarded = forwarded;
    }

    public void setAttachmentPack(AttachmentPack attachmentPack) {
        this.attachmentPack = attachmentPack;
    }

    public AttachmentPack getAttachmentPack() {
        return attachmentPack;
    }

    public void setDeleting(boolean deleting) {
        this.deleting = deleting;
    }

    public boolean isDeleting() {
        return deleting;
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

    private static volatile long tempId = Long.MIN_VALUE;

    public static long getUniqueTempId() {
        return ++tempId;
    }

    @Override
    public String toString() {
        return "Message from [" + correspondentId + "]. \"" + text + "\"";
    }

    public boolean hasFwd() {
        return forwarded != null && forwarded.size() > 0;
    }

    public boolean hasLoc() {
        return location != null;
    }

    public boolean hasAnyAttaches() {
        return hasAttaches() || hasLoc() || hasFwd();
    }

    public boolean hasAttaches() {
        return attachmentPack != null && attachmentPack.size() > 0;
    }
}
