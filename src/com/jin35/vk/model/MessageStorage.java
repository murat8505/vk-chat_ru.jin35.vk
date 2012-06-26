package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v4.util.LruCache;

import com.jin35.vk.model.db.DB;

public class MessageStorage implements IMessageStorage {

    private static final int MAX_DIALOGS_COUNT = 100;

    private static IMessageStorage instance;

    /**
     * [correspondentId, [messageId, Message]]
     * */
    private final LruCache<Long, Map<Long, Message>> messages = new LruCache<Long, Map<Long, Message>>(MAX_DIALOGS_COUNT) {
        @Override
        protected java.util.Map<Long, Message> create(Long key) {
            return new HashMap<Long, Message>();
        }
    };

    private final Timer timer = new Timer(true);
    /**
     * [uid, last typing time]
     */
    private final Map<Long, Long> typingUsers = new HashMap<Long, Long>();
    private final List<Message> selected = new ArrayList<Message>();

    private MessageStorage() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (typingUsers) {
                    long now = System.currentTimeMillis();
                    List<Long> trash = new ArrayList<Long>();
                    for (Entry<Long, Long> pair : typingUsers.entrySet()) {
                        if (now - pair.getValue() > 7000) {
                            trash.add(pair.getKey());
                        }
                    }
                    if (trash.size() > 0) {
                        for (Long id : trash) {
                            typingUsers.remove(id);
                        }
                        notifyConversationChanged(trash);
                    }
                }
            }
        }, 1000, 1000);
    }

    public synchronized static IMessageStorage getInstance() {
        if (instance == null) {
            instance = new MessageStorage();
        }
        return instance;
    }

    @Override
    public synchronized List<Message> getLastMessages() {
        List<Message> result = new ArrayList<Message>();

        for (Map<Long, Message> map : messages.snapshot().values()) {
            if (map.values().size() > 0) {
                List<Message> msgs = new ArrayList<Message>(map.values());
                Collections.sort(msgs, Message.getAscendingTimeComparator());
                result.add(msgs.get(0));
            }
        }
        return result;
    }

    private synchronized void addMessageWithoutNotification(Message msg) {
        Map<Long, Message> map = messages.get(msg.getCorrespondentId());
        if (!map.containsKey(msg.getId())) {
            map.put(msg.getId(), msg);
        }
    }

    @Override
    public synchronized void addMessage(Message msg) {
        addMessageWithoutNotification(msg);
        notifyConversationChanged(new Long[] { msg.getCorrespondentId() });
        notifyMessageChanged();
    }

    @Override
    public synchronized void addMessages(List<Message> msgs) {
        List<Long> uids = new ArrayList<Long>();
        for (Message m : msgs) {
            addMessageWithoutNotification(m);
            if (!uids.contains(m.getCorrespondentId())) {
                uids.add(m.getCorrespondentId());
            }
        }
        notifyConversationChanged(uids);
        notifyMessageChanged();
    }

    private void notifyMessageChanged() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_MESSAGES);
    }

    private void notifyConversationChanged(Long[] ids) {
        notifyConversationChanged(Arrays.asList(ids));
    }

    private void notifyConversationChanged(List<Long> ids) {
        NotificationCenter.getInstance().notifyConversationListeners(ids);
    }

    private void notifySelectedChanged() {
        NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_SELECTED);
    }

    @Override
    public synchronized List<Message> getMessagesWithUser(long uid) {
        Map<Long, Message> messageWithUser = messages.get(uid);
        if (messageWithUser == null) {
            return new ArrayList<Message>();
        }
        return new ArrayList<Message>(messageWithUser.values());
    }

    private synchronized void removeMessageWithoutNotification(Message msg) {
        Map<Long, Message> map = messages.get(msg.getCorrespondentId());
        map.remove(msg.getId());
    }

    @Override
    public synchronized void setSelection(Message msg, boolean selected) {
        if (selected && !this.selected.contains(msg)) {
            this.selected.add(msg);
        }
        if (!selected) {
            this.selected.remove(msg);
        }
        notifySelectedChanged();
    }

    @Override
    public synchronized List<Message> getSelected() {
        return selected;
    }

    @Override
    public synchronized List<Message> clearSelected() {
        List<Message> removed = new ArrayList<Message>(selected);
        selected.clear();
        notifySelectedChanged();
        return removed;
    }

    @Override
    public synchronized boolean isSelected(Message msg) {
        return selected.contains(msg);
    }

    @Override
    public synchronized boolean hasUnreadMessagesFromUser(long uid) {
        for (Message msg : getMessagesWithUser(uid)) {
            if (msg.isIncome() && !msg.isRead()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Message getMessageById(long mid) {
        Collection<Map<Long, Message>> allMessages = messages.snapshot().values();
        for (Map<Long, Message> map : allMessages) {
            Message result = map.get(mid);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public synchronized void deleteMessage(List<Message> msgs) {
        if (msgs.size() > 0) {
            List<Long> uids = new ArrayList<Long>();
            for (Message msg : msgs) {
                Long uid = msg.getCorrespondentId();
                Map<Long, Message> map = messages.get(uid);
                if (map.remove(msg.getId()) != null && !uids.contains(uid)) {
                    uids.add(uid);
                }
            }
            dump();
            notifyConversationChanged(uids);
            notifyMessageChanged();
        }
    }

    @Override
    public synchronized void deleteMessage(long mid) {
        Message msg = getMessageById(mid);
        if (msg != null) {
            Long uid = msg.getCorrespondentId();
            Map<Long, Message> map = messages.get(uid);
            map.remove(mid);
            notifyConversationChanged(new Long[] { uid });
            notifyMessageChanged();
            dump();
        }
    }

    @Override
    public synchronized int getUreadMessageCount() {
        int result = 0;
        Collection<Map<Long, Message>> allMessages = messages.snapshot().values();
        for (Map<Long, Message> map : allMessages) {
            for (Message msg : map.values()) {
                if (msg.isIncome() && !msg.isRead()) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public synchronized void messageSent(long uid, String text, Long tmpMid, Date confirmedDate, long confirmedMid, boolean read) {
        Message msg = null;
        if (tmpMid != null) {
            msg = getMessageByIdWithUser(tmpMid, uid);
            updateMessageId(msg, confirmedMid);
        }
        if (msg == null) {
            msg = getMessageByIdWithUser(confirmedMid, uid);
        }
        if (msg == null) {
            for (Message message : getMessagesWithUser(uid)) {
                boolean messageForUpdate = true;
                messageForUpdate &= message.getId() < 0;
                messageForUpdate &= (text == null && message.getText() == null) || (text != null && text.equals(message.getText()));
                if (messageForUpdate) {
                    tmpMid = message.getId();
                    updateMessageId(message, confirmedMid);
                    msg = message;
                    break;
                }
            }
        }
        if (msg == null) {
            return;
        }
        if (confirmedDate != null) {
            msg.setTime(confirmedDate);
        }
        if (read) {
            msg.setRead(true);
        }
        msg.setSent(true);
        msg.notifyChanges();
        if (tmpMid != null) {
            NotificationCenter.getInstance().notifyObjectListeners(tmpMid);
        }
        DB.getInstance().saveMessage(msg);
    }

    private Message getMessageByIdWithUser(long mid, long uid) {
        for (Message msg : getMessagesWithUser(uid)) {
            if (msg.getId() == mid) {
                return msg;
            }
        }
        return null;
    }

    private void updateMessageId(Message message, long newId) {
        removeMessageWithoutNotification(message);
        message.setId(newId);
        addMessageWithoutNotification(message);
    }

    @Override
    public synchronized void dump() {
        DB.getInstance().dumpMessages(messages.snapshot().values());
    }

    @Override
    public synchronized void markUserTyping(Long uid) {
        boolean needNotify = typingUsers.remove(uid) == null;
        typingUsers.put(uid, System.currentTimeMillis());
        if (needNotify) {
            notifyConversationChanged(new Long[] { uid });
        }
    }

    @Override
    public synchronized boolean isUserTyping(Long uid) {
        return typingUsers.get(uid) != null;
    }
}
