package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.util.LruCache;

public class MessageStorage {

    private static MessageStorage instance;

    private final LruCache<Long, Map<Long, Message>> messages = new LruCache<Long, Map<Long, Message>>(100) {
        @Override
        protected java.util.Map<Long, Message> create(Long key) {
            return new HashMap<Long, Message>();
        }
    };

    private MessageStorage() {
    }

    public static MessageStorage getInstance() {
        if (instance == null) {
            instance = new MessageStorage();
        }
        return instance;
    }

    public synchronized List<Message> getLastMessages() {
        List<Message> result = new ArrayList<Message>();
        System.out.println("getLastMessages");

        for (Map<Long, Message> map : messages.snapshot().values()) {
            if (map.values().size() > 0) {
                List<Message> msgs = new ArrayList<Message>(map.values());
                Collections.sort(msgs, getComparator());
                result.add(msgs.get(0));
            }
        }
        System.out.println("last messages size: " + result.size());
        return result;
    }

    private Comparator<Message> getComparator() {
        return new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return lhs.getTime().compareTo(rhs.getTime());
            }
        };
    }

    private synchronized void addMessageWithoutNotification(Message msg) {
        Map<Long, Message> map = messages.get(msg.getCorrespondentId());
        map.remove(msg.getId());
        map.put(msg.getId(), msg);
    }

    public synchronized void addMessage(Message msg) {
        addMessageWithoutNotification(msg);
        notifyModelChanged();
    }

    public synchronized void addMessages(List<Message> msgs) {
        for (Message m : msgs) {
            addMessageWithoutNotification(m);
        }
        notifyModelChanged();
    }

    private void notifyModelChanged() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_MESSAGES);
    }
}
