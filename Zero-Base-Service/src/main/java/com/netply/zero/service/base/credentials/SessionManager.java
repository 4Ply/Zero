package com.netply.zero.service.base.credentials;

public class SessionManager {
    private static String sessionKey;
    private static Integer clientID;


    public static String getSessionKey() {
        return sessionKey;
    }

    public static void setSessionKey(String sessionKey) {
        SessionManager.sessionKey = sessionKey;
    }

    public static void setClientID(Integer clientID) {
        SessionManager.clientID = clientID;
    }

    public static Integer getClientID() {
        return clientID;
    }
}
