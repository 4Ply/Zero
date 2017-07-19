package com.netply.zero.service.base.messaging;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.ToUserMessage;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.sun.jersey.api.client.ClientResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageListener {
    private String botChanURL;
    private String platform;


    public MessageListener(String botChanURL, String platform) {
        this.botChanURL = botChanURL;
        this.platform = platform;
    }

    public void checkMessages(String url, MatcherList matcherList, final Consumer<Message> messageConsumer) {
        checkSubscribedObjects(url, matcherList, output -> {
            List<Message> messages = ListUtil.stringToArray(output, Message[].class);
            for (Message message : messages) {
                System.out.println(message.toString());
                deleteMessage(message, messageConsumer);
            }

            System.out.println("Messages Parsed: " + output);
        });
    }

    public void checkReplies(String url, MatcherList matcherList, final Consumer<ToUserMessage> replyConsumer) {
        checkSubscribedObjects(url, matcherList, output -> {
            List<ToUserMessage> messages = ListUtil.stringToArray(output, ToUserMessage[].class);
            for (ToUserMessage toUserMessage : messages) {
                System.out.println(toUserMessage.toString());
                deleteReply(toUserMessage, replyConsumer);
            }

            System.out.println("Replies Parsed: " + output);
        });
    }

    public void checkSubscribedObjects(String url, MatcherList matcherList, Consumer<String> successConsumer) {
        try {
            Service.create(botChanURL).post(url, new BasicSessionCredentials(), matcherList, null, new ServiceCallback<ArrayList>() {
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
        String deleteMessageURL = String.format("/message?platform=%s&id=%s", platform, message.getId());
        Service.create(botChanURL).delete(deleteMessageURL, new BasicSessionCredentials(), new ServiceCallback<Object>() {
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

    private void deleteReply(ToUserMessage toUserMessage, Consumer<ToUserMessage> replyConsumer) {
        String deleteMessageURL = String.format("/reply?platform=%s&id=%s", platform, toUserMessage.getId());
        Service.create(botChanURL).delete(deleteMessageURL, new BasicSessionCredentials(), new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                replyConsumer.accept(toUserMessage);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
