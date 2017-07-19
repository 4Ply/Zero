package com.netply.zero.service.base.credentials;

public class SessionManager {
    private static String sessionKey;
    private static Integer nodeID;


    public static String getSessionKey() {
        return sessionKey;
    }

    public static void setSessionKey(String sessionKey) {
        SessionManager.sessionKey = sessionKey;
    }

    public static void setNodeID(Integer nodeID) {
        SessionManager.nodeID = nodeID;
    }

    public static Integer getNodeID() {
        return nodeID;
    }
}
