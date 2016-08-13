package com.netply.zero.service.base.messaging;

import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import com.sun.jersey.api.client.ClientResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageListener {
    private String botChanURL;


    public MessageListener(String botChanURL) {
        this.botChanURL = botChanURL;
    }

    public void checkMessages(String url, final Consumer<Message> messageConsumer) {
        checkSubscribedObjects(url, output -> {
            List<Message> messages = ListUtil.stringToArray(output, Message[].class);
            for (Message message : messages) {
                System.out.println(message.toString());
                deleteMessage(message, messageConsumer);
            }

            System.out.println("Messages Parsed: " + output);
        });
    }

    public void checkReplies(String url, final Consumer<Reply> replyConsumer) {
        checkSubscribedObjects(url, output -> {
            List<Reply> messages = ListUtil.stringToArray(output, Reply[].class);
            for (Reply reply : messages) {
                System.out.println(reply.toString());
                deleteReply(reply, replyConsumer);
            }

            System.out.println("Replies Parsed: " + output);
        });
    }

    private void checkSubscribedObjects(String url, Consumer<String> successConsumer) {
        try {
            Service.create(botChanURL).get(url, false, new BasicSessionCredentials(), null, new ServiceCallback<ArrayList>() {
                @Override
                public void onError(ClientResponse response) {
                    Logger.getGlobal().log(Level.SEVERE, "Failed to get messages: " + response.getStatus());
                    Logger.getGlobal().log(Level.SEVERE, response.toString());
                }

                @Override
                public void onSuccess(String output) {
                    successConsumer.accept(output);
                }

                @Override
                public void onSuccess(ArrayList parsedResponse) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(Message message, Consumer<Message> messageConsumer) {
        String deleteMessageURL = String.format("/message?clientID=%s&id=%s", String.valueOf(SessionManager.getClientID()), message.getId());
        Service.create(botChanURL).delete(deleteMessageURL, false, new BasicSessionCredentials(), new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                messageConsumer.accept(message);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    private void deleteReply(Reply reply, Consumer<Reply> replyConsumer) {
        String deleteMessageURL = String.format("/reply?clientID=%s&id=%s", String.valueOf(SessionManager.getClientID()), reply.getId());
        Service.create(botChanURL).delete(deleteMessageURL, false, new BasicSessionCredentials(), new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                replyConsumer.accept(reply);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
