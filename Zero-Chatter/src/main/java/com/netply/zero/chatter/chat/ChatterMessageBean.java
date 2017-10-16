package com.netply.zero.chatter.chat;

import com.netply.botchan.web.model.FromUserMessage;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class ChatterMessageBean {
    private String botChanURL;
    private MessageListener messageListener;
    private Map<String, Consumer<FromUserMessage>> messageMatchers;


    @Autowired
    public ChatterMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(this.botChanURL);
        initMessageMatchers();
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.GENERATE_PLATFORM_OTP, this::generatePlatformOTP);
        messageMatchers.put(ChatMatchers.ALLOW_PLATFORM_OTP, this::allowPlatformOTP);
        messageMatchers.put(ChatMatchers.LINK_PLATFORM, this::linkPlatform);
        messageMatchers.put(ChatMatchers.KYS, this::kys);
    }

    private void generatePlatformOTP(FromUserMessage message) {
        Service.create(botChanURL).get(String.format("/platformOTP?platformID=%s", message.getPlatformID()), String.class, new ServiceCallback<String>() {
            @Override
            public void onError(ClientResponse response) {
                MessageUtil.reply(botChanURL, message, "Failed to generate OTP");
            }

            @Override
            public void onSuccess(String output) {
                MessageUtil.reply(botChanURL, message, "OTP: " + output);
            }
        });
    }

    private void allowPlatformOTP(FromUserMessage message) {
        String messageText = removeMatcherText(message.getMessage(), ChatMatchers.ALLOW_PLATFORM_OTP).trim();

        Service.create(botChanURL).get(String.format("/userOTP?platformID=%s&platformOTP=%s", message.getPlatformID(), messageText), String.class, new ServiceCallback<String>() {
            @Override
            public void onError(ClientResponse response) {
                MessageUtil.reply(botChanURL, message, "Failed to generate OTP");
            }

            @Override
            public void onSuccess(String output) {
                MessageUtil.reply(botChanURL, message, "OTP: " + output);
            }
        });
    }

    private void linkPlatform(FromUserMessage message) {
        String messageText = removeMatcherText(message.getMessage(), ChatMatchers.LINK_PLATFORM).trim();

        if (!messageText.isEmpty()) {
            Service.create(botChanURL).get(String.format("/linkPlatform?&platformID=%s&userOTP=%s", message.getPlatformID(), messageText), String.class, new ServiceCallback<String>() {
                @Override
                public void onError(ClientResponse response) {
                    MessageUtil.reply(botChanURL, message, "Failed to link platform");
                }

                @Override
                public void onSuccess(String output) {
                    MessageUtil.reply(botChanURL, message, "Linked! " + output);
                }
            });
        }
    }

    private String removeMatcherText(String messageText, String matcher) {
        return messageText.replaceAll(matcher.replace("(.*)", "").replace("(.*)", ""), "").trim();
    }

    private void kys(FromUserMessage message) {
        if (message.isDirect()) {
            MessageUtil.reply(botChanURL, message, ":(");
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMessages() {
        messageListener.checkMessages("/messages", new ArrayList<>(messageMatchers.keySet()), this::parseMessage);
    }

    private void parseMessage(FromUserMessage message) {
        messageMatchers.keySet().stream()
                .filter(message.getMessage()::matches)
                .map(messageMatchers::get)
                .forEach(messageConsumer -> messageConsumer.accept(message));
    }
}
