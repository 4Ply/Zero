package com.netply.zero.service.base;

import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.zero.service.base.credentials.SessionManager;
import com.sun.jersey.api.client.ClientResponse;

public class BasicLoginCallback implements ServiceCallback<BasicResultResponse> {
    @Override
    public void onError(ClientResponse response) {
        int status = response != null ? response.getStatus() : -1;
        System.exit(-1);
        throw new RuntimeException("Unable to authenticate with Bot-chan! Error code: " + status);
    }

    @Override
    public void onSuccess(String output) {

    }

    @Override
    public void onSuccess(BasicResultResponse parsedResponse) {
        SessionManager.setSessionKey(parsedResponse.getSessionKey());
        SessionManager.setNodeID(parsedResponse.getClientID());
    }
}
