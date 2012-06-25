package com.jin35.vk.model.db;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class DB implements IDB {

    private final SQLiteDatabase db;
    private static IDB instance;

    private DB(Context context) {
        db = new DBConnection(context).getReadableDatabase();
    }

    public static void init(Context context) {
        instance = new DB(context);
    }

    public static IDB getInstance() {
        return instance;
    }

    private static final String PHOTO_TABLE = "PHOTO_TABLE";
    private static final String PHOTO_URL = "PHOTO_URL";
    private static final String PHOTO_BLOB = "PHOTO_BLOB";

    static final String SQL_CREATE_PHOTO_TABLE = "CREATE TABLE " + PHOTO_TABLE + " (" + PHOTO_URL + " VARCHAR NOT NULL, " + PHOTO_BLOB + " BLOB, PRIMARY KEY ("
            + PHOTO_URL + "));";

    private final static String WHERE_URL = PHOTO_URL + " = ?";

    @Override
    public Bitmap getPhoto(String photoUrl) {
        Cursor c = db.query(PHOTO_TABLE, new String[] { PHOTO_BLOB }, WHERE_URL, new String[] { photoUrl }, null, null, null);
        Bitmap result = null;
        try {
            if (c.moveToFirst()) {
                byte[] blob = c.getBlob(0);
                result = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            }
        } finally {
            c.close();
        }
        return result;
    }

    @Override
    public void savePhoto(String photoUrl, Bitmap photo) {
        db.beginTransaction();
        try {
            db.delete(PHOTO_TABLE, WHERE_URL, new String[] { photoUrl });
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(CompressFormat.PNG, 100, baos);
            byte[] blob = baos.toByteArray();
            ContentValues cv = new ContentValues();
            cv.put(PHOTO_URL, photoUrl);
            cv.put(PHOTO_BLOB, blob);
            db.insert(PHOTO_TABLE, null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static final String USERS_TABLE = "USERS_TABLE";
    private static final String USER_ID = "USER_ID";
    private static final String USER_NAME = "USER_NAME";
    private static final String USER_FAMILIY_NAME = "USER_FAMILIY_NAME";
    private static final String USER_PHOTO_URL = "USER_PHOTO_URL";

    private static final String FRIENDS_TABLE = "FRIENDS_TABLE";
    private static final String REQUESTS_TABLE = "REQUESTS_TABLE";

    static final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + USERS_TABLE + " (" + USER_ID + " INTEGER NOT NULL, " + USER_NAME + " VARCHAR, "
            + USER_FAMILIY_NAME + " VARCHAR, " + USER_PHOTO_URL + " VARCHAR, PRIMARY KEY (" + USER_ID + "));";

    static final String SQL_CREATE_FRIENDS_TABLE = "CREATE TABLE " + FRIENDS_TABLE + " (" + USER_ID + " INTEGER NOT NULL, PRIMARY KEY (" + USER_ID + "));";
    static final String SQL_CREATE_REQUESTS_TABLE = "CREATE TABLE " + REQUESTS_TABLE + " (" + USER_ID + " INTEGER NOT NULL, PRIMARY KEY (" + USER_ID + "));";

    @Override
    public void dumpUsersLists(Map<Long, UserInfo> users, List<Long> friends, List<Long> requests) {
        db.beginTransaction();
        try {
            db.delete(USERS_TABLE, "1", null);
            db.delete(FRIENDS_TABLE, "1", null);
            db.delete(REQUESTS_TABLE, "1", null);

            for (UserInfo user : users.values()) {
                ContentValues cv = new ContentValues();
                cv.put(USER_ID, user.getId());
                cv.put(USER_NAME, user.getName());
                cv.put(USER_FAMILIY_NAME, user.getFamilyName());
                cv.put(USER_PHOTO_URL, user.getPhotoUrl());
                db.insert(USERS_TABLE, null, cv);
            }
            for (Long id : friends) {
                ContentValues cv = new ContentValues();
                cv.put(USER_ID, id);
                db.insert(FRIENDS_TABLE, null, cv);
            }
            for (Long id : requests) {
                ContentValues cv = new ContentValues();
                cv.put(USER_ID, id);
                db.insert(REQUESTS_TABLE, null, cv);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void cacheUsers() {
        Cursor c = db.query(USERS_TABLE, new String[] { USER_ID, USER_NAME, USER_FAMILIY_NAME, USER_PHOTO_URL }, "1", null, null, null, null);
        try {
            int idColumnIndex = c.getColumnIndex(USER_ID);
            int nameColumnIndex = c.getColumnIndex(USER_NAME);
            int surnameColumnIndex = c.getColumnIndex(USER_FAMILIY_NAME);
            int urlColumnIndex = c.getColumnIndex(USER_PHOTO_URL);
            while (c.moveToNext()) {
                UserInfo user = new UserInfo(c.getLong(idColumnIndex));
                user.setName(c.getString(nameColumnIndex));
                user.setFamilyName(c.getString(surnameColumnIndex));
                user.setPhotoUrl(c.getString(urlColumnIndex));
                UserStorageFactory.getInstance().getUserStorage().putNewUser(user);
            }
        } finally {
            c.close();
        }
        UserStorageFactory.getInstance().getUserStorage().markAsFriend(getIdsFromTable(FRIENDS_TABLE));
        UserStorageFactory.getInstance().getUserStorage().markAsRequest(getIdsFromTable(REQUESTS_TABLE));
    }

    private List<Long> getIdsFromTable(String tableName) {
        List<Long> ids = new ArrayList<Long>();
        Cursor c = db.query(tableName, new String[] { USER_ID }, "1", null, null, null, null);
        try {
            int idColumnIndex = c.getColumnIndex(USER_ID);
            while (c.moveToNext()) {
                ids.add(c.getLong(idColumnIndex));
            }

        } finally {
            c.close();
        }
        return ids;
    }

    private static final String MESSAGES_TABLE = "MESSAGES_TABLE";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String CORRESPONDENT_ID = "CORRESPONDENT_ID";
    private static final String MESSAGE_TEXT = "MESSAGE_TEXT";
    private static final String MESSAGE_TIME = "MESSAGE_TIME";
    private static final String MESSAGE_INCOME = "MESSAGE_INCOME";

    static final String SQL_CREATE_MESSAGES_TABLE = "CREATE TABLE " + MESSAGES_TABLE + " (" + MESSAGE_ID + " INTEGER NOT NULL, " + CORRESPONDENT_ID
            + " INTEGER NOT NULL, " + MESSAGE_TEXT + " VARCHAR, " + MESSAGE_TIME + " INTEGER NOT NULL, " + MESSAGE_INCOME + " INTEGER NOT NULL);";
    // , PRIMARY KEY (" + MESSAGE_ID + ")

    private static final String WHERE_MESSAGE_ID = MESSAGE_ID + " = ?";

    @Override
    public void dumpMessages(Collection<Map<Long, Message>> messages) {
        db.beginTransaction();
        try {
            db.delete(MESSAGES_TABLE, "1", null);

            for (Map<Long, Message> map : messages) {
                for (Message msg : map.values()) {
                    if (msg.getId() < 0 || (!msg.isIncome() && !msg.isSent())) {
                        continue;
                    }
                    insertMessage(msg);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertMessage(Message msg) {
        ContentValues cv = new ContentValues();
        cv.put(MESSAGE_ID, msg.getId());
        cv.put(CORRESPONDENT_ID, msg.getCorrespondentId());
        cv.put(MESSAGE_TEXT, msg.getText());
        cv.put(MESSAGE_TIME, msg.getTime().getTime());
        cv.put(MESSAGE_INCOME, msg.isIncome() ? 1 : 0);
        db.insert(MESSAGES_TABLE, null, cv);
    }

    @Override
    public void saveMessage(Message message) {
        if (message.getId() < 0) {
            return;
        }
        db.beginTransaction();
        try {
            db.delete(MESSAGES_TABLE, WHERE_MESSAGE_ID, new String[] { String.valueOf(message.getId()) });
            insertMessage(message);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void cacheMessages() {
        Cursor c = db.query(MESSAGES_TABLE, new String[] { MESSAGE_ID, CORRESPONDENT_ID, MESSAGE_TEXT, MESSAGE_TIME, MESSAGE_INCOME }, "1", null, null, null,
                null);
        try {
            int msgIdIndex = c.getColumnIndex(MESSAGE_ID);
            int corrIdIndex = c.getColumnIndex(CORRESPONDENT_ID);
            int msgTimeIndex = c.getColumnIndex(MESSAGE_TIME);
            int msgTextIndex = c.getColumnIndex(MESSAGE_TEXT);
            int msgIncomeIndex = c.getColumnIndex(MESSAGE_INCOME);
            List<Message> msgs = new ArrayList<Message>();
            while (c.moveToNext()) {
                Message msg = new Message(c.getLong(msgIdIndex), c.getLong(corrIdIndex), c.getString(msgTextIndex), new Date(c.getLong(msgTimeIndex)),
                        c.getInt(msgIncomeIndex) == 1);
                msg.setRead(true);
                msgs.add(msg);
            }
            MessageStorage.getInstance().addMessages(msgs);
        } finally {
            c.close();
        }
    }
}
