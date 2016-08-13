package com.netply.zero.service.base.credentials;

public interface ZeroCredentials {
    String getUsername();

    String getSessionKey();

    String getPasswordHash();
}
