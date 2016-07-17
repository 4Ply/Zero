package com.netply.zero.service.base;

public interface ZeroCredentials {
    String getUsername();

    String getSessionKey();

    String getPasswordHash();

    String getBotType();
}
