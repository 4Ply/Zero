package com.netply.zero.eventador.league.chat;

import com.netply.botchan.web.model.Message;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LeagueMessageBean {
    private String botChanURL;


    @Autowired
    public LeagueMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void checkForLeagueMessages() {
        try {
            String url = String.format("/messages?id=%s", String.valueOf(SessionManager.getClientID()));
            Service.create(botChanURL).get(url, false, new BasicSessionCredentials(), null, new ServiceCallback<ArrayList>() {
                @Override
                public void onError(ClientResponse response) {
                    Logger.getGlobal().log(Level.SEVERE, "Failed to get messages: " + response.getStatus());
                    Logger.getGlobal().log(Level.SEVERE, response.toString());
                }

                @Override
                public void onSuccess(String output) {
                    List<Message> messages = ListUtil.stringToArray(output, Message[].class);
                    for (Message message : messages) {
                        System.out.println(message.toString());
                        deleteMessage(message.getId());
                    }

                    System.out.println("Messages: " + output);
                }

                @Override
                public void onSuccess(ArrayList parsedResponse) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(String messageID) {
        String deleteMessageURL = String.format("/message?clientID=%s&id=%s", String.valueOf(SessionManager.getClientID()), messageID);
        Service.create(botChanURL).delete(deleteMessageURL, false, new BasicSessionCredentials(), new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {

            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
