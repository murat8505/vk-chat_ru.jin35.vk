package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import android.support.v4.util.LruCache;

import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.Token;

public class MessageStorage implements IMessageStorage {

    private static final int MAX_DIALOGS_COUNT = 10000;

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

    /**
     * [chatId, [messageId, Message]]
     * */
    private final LruCache<Long, Map<Long, ChatMessage>> chatMessages = new LruCache<Long, Map<Long, ChatMessage>>(MAX_DIALOGS_COUNT) {
        @Override
        protected java.util.Map<Long, ChatMessage> create(Long key) {
            return new HashMap<Long, ChatMessage>();
        }
    };

    /**
     * [uid, last typing time]
     */
    private final Map<Long, Long> typingUsers = new HashMap<Long, Long>();

    /**
     * [chatId, [uid, last typing time]]
     */
    private final Map<Long, Map<Long, Long>> typingChatUsers = new HashMap<Long, Map<Long, Long>>();

    private final List<Message> selected = new ArrayList<Message>();

    private boolean hasMoreDialogs = true;
    private int downloadedDialogCount = 0;
    private final List<Long> usersWithFullHistory = new ArrayList<Long>();
    private final List<Long> chatsWithFullHistory = new ArrayList<Long>();
    private final Map<Long, Integer> downloadedMessageCount = new HashMap<Long, Integer>();
    private final Map<Long, Integer> downloadedChatMessageCount = new HashMap<Long, Integer>();

    private MessageStorage() {
        Token.getInstance().getTimer().schedule(new TimerTask() {
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
                    Map<Long, Long> chatTrash = new HashMap<Long, Long>();
                    for (Entry<Long, Map<Long, Long>> pair : typingChatUsers.entrySet()) {
                        for (Entry<Long, Long> pair2 : pair.getValue().entrySet()) {
                            if (now - pair2.getValue() > 7000) {
                                chatTrash.put(pair.getKey(), pair2.getKey());
                            }
                        }
                    }
                    if (chatTrash.size() > 0) {
                        for (Entry<Long, Long> ids : chatTrash.entrySet()) {
                            Map<Long, Long> map = typingChatUsers.get(ids.getKey());
                            map.remove(ids.getValue());
                            if (map.size() == 0) {
                                typingChatUsers.remove(ids.getKey());
                            }
                        }
                        notifyConversationChanged(new ArrayList<Long>(chatTrash.keySet()));
                    }
                }
            }
        }, 1000, 1000);
    }

    public static boolean init() {
        if (instance == null) {
            instance = new MessageStorage();
            return true;
        }
        return false;
    }

    public synchronized static IMessageStorage getInstance() {

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
        for (Map<Long, ChatMessage> map : chatMessages.snapshot().values()) {
            if (map.values().size() > 0) {
                List<Message> msgs = new ArrayList<Message>(map.values());
                Collections.sort(msgs, Message.getAscendingTimeComparator());
                result.add(msgs.get(0));
            }
        }
        return result;
    }

    private synchronized void addMessageWithoutNotification(Message msg) {
        if (msg instanceof ChatMessage) {
            Map<Long, ChatMessage> map = chatMessages.get(msg.getCorrespondentId());
            if (!map.containsKey(msg.getId())) {
                map.put(msg.getId(), (ChatMessage) msg);
            }
        } else {
            Map<Long, Message> map = messages.get(msg.getCorrespondentId());
            if (!map.containsKey(msg.getId())) {
                map.put(msg.getId(), msg);
            }
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

    @Override
    public synchronized List<ChatMessage> getMessagesFromChat(long chatId) {
        Map<Long, ChatMessage> messages = chatMessages.get(chatId);
        if (messages == null) {
            return new ArrayList<ChatMessage>();
        }
        return new ArrayList<ChatMessage>(messages.values());
    }

    private synchronized void removeMessageWithoutNotification(Message msg) {
        if (msg instanceof ChatMessage) {
            Map<Long, ChatMessage> map = chatMessages.get(msg.getCorrespondentId());
            map.remove(msg.getId());
        } else {
            Map<Long, Message> map = messages.get(msg.getCorrespondentId());
            map.remove(msg.getId());
        }
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
    public synchronized boolean hasUnreadMessagesFromChat(long chatId) {
        for (Message msg : getMessagesFromChat(chatId)) {
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

        Collection<Map<Long, ChatMessage>> allChatMessages = chatMessages.snapshot().values();
        for (Map<Long, ChatMessage> map : allChatMessages) {
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
                Map<Long, ChatMessage> chatMap = chatMessages.get(uid);
                if (chatMap.remove(msg.getId()) != null && !uids.contains(uid)) {
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
            Map<Long, ChatMessage> chatMap = chatMessages.get(uid);
            chatMap.remove(mid);
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
        Collection<Map<Long, ChatMessage>> allChatMessages = chatMessages.snapshot().values();
        for (Map<Long, ChatMessage> map : allChatMessages) {
            for (Message msg : map.values()) {
                if (msg.isIncome() && !msg.isRead()) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public synchronized void updateMsgId(Message msg, long newMid) {
        removeMessageWithoutNotification(msg);
        msg.setId(newMid);
        addMessageWithoutNotification(msg);

        msg.notifyChanges();
        notifyConversationChanged(new Long[] { msg.getCorrespondentId() });
        DB.getInstance().saveMessage(msg);
    }

    // @Override
    // public synchronized void messageSent(long uid, String text, Long tmpMid, Date confirmedDate, long confirmedMid, boolean read) {
    // Message msg = null;
    // if (tmpMid != null) {
    // msg = getMessageByIdWithUser(tmpMid, uid);
    // updateMessageId(msg, confirmedMid);
    // }
    // if (msg == null) {
    // msg = getMessageByIdWithUser(confirmedMid, uid);
    // }
    // if (msg == null) {
    // for (Message message : getMessagesWithUser(uid)) {
    // boolean messageForUpdate = true;
    // messageForUpdate &= message.getId() < 0;
    // messageForUpdate &= (text == null && message.getText() == null) || (text != null && text.equals(message.getText()));
    // if (messageForUpdate) {
    // tmpMid = message.getId();
    // updateMessageId(message, confirmedMid);
    // msg = message;
    // break;
    // }
    // }
    // }
    // if (msg == null) {
    // System.out.println("early return");
    // return;
    // }
    // System.out.println("msg sent method - confirm date: " + confirmedDate);
    // if (confirmedDate != null) {
    // System.out.println("msg sent method - update date");
    // msg.setTime(confirmedDate);
    // }
    // if (read) {
    // msg.setRead(true);
    // }
    // msg.setSent(true);
    // msg.notifyChanges();
    // notifyConversationChanged(new Long[] { uid });
    // System.out.println("msg sent method - notify changes");
    // DB.getInstance().saveMessage(msg);
    // }
    // private Message getMessageByIdWithUser(long mid, long uid) {
    // for (Message msg : getMessagesWithUser(uid)) {
    // if (msg.getId() == mid) {
    // return msg;
    // }
    // }
    // return null;
    // }
    //
    // private void updateMessageId(Message message, long newId) {
    // removeMessageWithoutNotification(message);
    // message.setId(newId);
    // addMessageWithoutNotification(message);
    // }

    @Override
    public synchronized void dump() {
        DB.getInstance().dumpMessages(messages.snapshot().values(), chatMessages.snapshot().values());
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
    public synchronized void markChatUserTyping(Long chatId, Long uid) {
        Map<Long, Long> map = typingChatUsers.get(chatId);
        if (map == null) {
            map = new HashMap<Long, Long>();
            typingChatUsers.put(chatId, map);
        }
        boolean needNotify = map.remove(uid) == null;
        map.put(uid, System.currentTimeMillis());
        if (needNotify) {
            notifyConversationChanged(new Long[] { chatId });
        }
    }

    @Override
    public synchronized boolean isUserTyping(Long uid) {
        return typingUsers.get(uid) != null;
    }

    @Override
    public synchronized List<Long> getUsersTyping(Long chatId) {
        Map<Long, Long> map = typingChatUsers.get(chatId);
        if (map != null) {
            return new ArrayList<Long>(map.keySet());
        } else {
            return new ArrayList<Long>();
        }
    }

    @Override
    public void setDownloadedDialogCount(int count) {
        if (count == 0) {
            hasMoreDialogs = false;
        }
        downloadedDialogCount += count;
    }

    @Override
    public void setMessagesWithUserCount(Long uid, int count) {
        if (count == 0) {
            if (!usersWithFullHistory.contains(uid)) {
                usersWithFullHistory.add(uid);
            }
        }
        Integer i = downloadedMessageCount.remove(uid);
        if (i == null) {
            i = 0;
        }
        downloadedMessageCount.put(uid, i + count);
    }

    @Override
    public void setChatMessagesCount(Long chatId, int count) {
        if (count == 0) {
            if (!chatsWithFullHistory.contains(chatId)) {
                chatsWithFullHistory.add(chatId);
            }
        }
        Integer i = downloadedChatMessageCount.remove(chatId);
        if (i == null) {
            i = 0;
        }
        downloadedChatMessageCount.put(chatId, i + count);
    }

    @Override
    public int getDownloadedDialogCount() {
        return downloadedDialogCount;
    }

    @Override
    public int getDownloadedMessageCount(Long uid) {
        Integer i = downloadedMessageCount.get(uid);
        if (i == null) {
            return 0;
        }
        return i;
    }

    @Override
    public int getDownloadedChatMessageCount(Long chatId) {
        Integer i = downloadedChatMessageCount.get(chatId);
        if (i == null) {
            return 0;
        }
        return i;
    }

    @Override
    public boolean hasMoreDialogs() {
        return hasMoreDialogs;
    }

    @Override
    public boolean hasMoreMessagesWithUser(Long uid) {
        return !usersWithFullHistory.contains(uid);
    }

    @Override
    public boolean hasMoreChatMessages(Long chatId) {
        return !chatsWithFullHistory.contains(chatId);
    }
}
