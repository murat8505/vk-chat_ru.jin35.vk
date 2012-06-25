package com.jin35.vk.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBConnection extends SQLiteOpenHelper {

    private final static String DB_NAME = "VKMessaenger";
    private final static int DB_VERSION = 1;

    public DBConnection(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB.SQL_CREATE_PHOTO_TABLE);
        db.execSQL(DB.SQL_CREATE_USERS_TABLE);
        db.execSQL(DB.SQL_CREATE_FRIENDS_TABLE);
        db.execSQL(DB.SQL_CREATE_REQUESTS_TABLE);
        db.execSQL(DB.SQL_CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
