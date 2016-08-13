package com.netply.zero.service.base.credentials;

public class BasicSessionCredentials implements ZeroCredentials {
    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getSessionKey() {
        return SessionManager.getSessionKey();
    }

    @Override
    public String getPasswordHash() {
        return null;
    }
}
