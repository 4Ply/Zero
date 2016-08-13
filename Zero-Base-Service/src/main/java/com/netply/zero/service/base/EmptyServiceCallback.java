package com.netply.zero.service.base;

import com.sun.jersey.api.client.ClientResponse;

public class EmptyServiceCallback<T> implements ServiceCallback<T> {
    @Override
    public void onError(ClientResponse response) {

    }

    @Override
    public void onSuccess(String output) {

    }

    @Override
    public void onSuccess(Object parsedResponse) {

    }
}
