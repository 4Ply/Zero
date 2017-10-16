package com.netply.zero.service.base.messaging;

import com.netply.botchan.web.model.FromUserMessage;
import com.netply.botchan.web.model.MatcherListWrapper;
import com.netply.botchan.web.model.ToUserMessage;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MessageListener {
    private Logger logger = Logger.getLogger(this.getClass());
    private String botChanURL;


    public MessageListener(String botChanURL) {
        this.botChanURL = botChanURL;
    }

    public void checkMessages(String url, ArrayList<String> matcherList, final Consumer<FromUserMessage> messageConsumer) {
        checkSubscribedObjects(url, matcherList, output -> {
            List<FromUserMessage> messages = ListUtil.stringToArray(output, FromUserMessage[].class);
            for (FromUserMessage message : messages) {
                logger.info(message.toString());
                deleteMessage(message, messageConsumer);
            }

            logger.info("Messages Parsed: " + output);
        });
    }

    public void checkReplies(String url, ArrayList<String> matcherList, final Consumer<ToUserMessage> replyConsumer) {
        checkSubscribedObjects(url, matcherList, output -> {
            List<ToUserMessage> messages = ListUtil.stringToArray(output, ToUserMessage[].class);
            for (ToUserMessage toUserMessage : messages) {
                logger.info(toUserMessage.toString());
                deleteReply(toUserMessage, replyConsumer);
            }

            logger.info("Replies Parsed: " + output);
        });
    }

    public void checkSubscribedObjects(String url, ArrayList<String> matcherList, Consumer<String> successConsumer) {
        try {
            Service.create(botChanURL).post(url, new MatcherListWrapper(matcherList), null, new ServiceCallback<ArrayList>() {
                @Override
                public void onError(ClientResponse response) {
                    if (response != null) {
                        logger.fatal("Failed to get messages: " + response.getStatus());
                        logger.fatal(response.toString());
                    }
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

    private void deleteMessage(FromUserMessage message, Consumer<FromUserMessage> messageConsumer) {
        String deleteMessageURL = String.format("/message?&id=%s", message.getId());
        Service.create(botChanURL).delete(deleteMessageURL, new ServiceCallback<Object>() {
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
        String deleteReplyURL = String.format("/reply?&id=%s", toUserMessage.getId());
        Service.create(botChanURL).delete(deleteReplyURL, new ServiceCallback<Object>() {
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
