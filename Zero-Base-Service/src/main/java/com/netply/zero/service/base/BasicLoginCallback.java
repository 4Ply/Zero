package com.netply.zero.service.base;

import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.zero.service.base.credentials.SessionManager;
import com.sun.jersey.api.client.ClientResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicLoginCallback implements ServiceCallback<BasicResultResponse> {
    @Override
    public void onError(ClientResponse response) {
        Logger.getGlobal().log(Level.SEVERE, response.toString());
        System.exit(-response.getStatus());
        throw new RuntimeException("Unable to authenticate with Bot-chan! Error code: " + response.getStatus());
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
