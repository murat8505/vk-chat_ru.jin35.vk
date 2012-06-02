package com.jin35.vk.net;


public class Token {

    private static String currentToken = null;

    public static void setNewToken(String newToken) {
        currentToken = newToken;
    }

    public static String getToken() {
        return currentToken;
    }

}
