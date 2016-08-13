package com.netply.zero.service.base;

import com.sun.jersey.api.client.ClientResponse;

public interface ServiceCallback<T> {
    void onError(ClientResponse response);

    void onSuccess(String output);

    void onSuccess(T parsedResponse);
}
