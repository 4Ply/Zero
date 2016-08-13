package com.netply.zero.service.base;

public class SessionManager {
    private static String sessionKey;


    public static String getSessionKey() {
        return sessionKey;
    }

    public static void setSessionKey(String sessionKey) {
        SessionManager.sessionKey = sessionKey;
    }
}
