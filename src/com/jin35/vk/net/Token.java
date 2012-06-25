package com.jin35.vk.net;

import android.content.Context;

public class Token {

    private static final String TOKEN_PREF = "token";
    private static final String SECURITY_PREFS = "SECURITY_PREFS";

    private final Context context;
    private static Token instance;
    private String currentToken = null;

    private Token(Context context) {
        this.context = context;
        currentToken = context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).getString(TOKEN_PREF, null);
    }

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new Token(context);
        }
    }

    public static Token getInstance() {
        return instance;
    }

    public void setNewToken(String newToken) {
        currentToken = newToken;
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).edit().putString(TOKEN_PREF, newToken).commit();
    }

    public String getToken() {
        return currentToken;
    }

}
