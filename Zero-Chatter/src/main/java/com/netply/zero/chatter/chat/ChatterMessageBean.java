package com.netply.zero.chatter.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
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
    private String platform;
    private MessageListener messageListener;
    private Map<String, Consumer<Message>> messageMatchers;


    @Autowired
    public ChatterMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.platform}") String platform) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        messageListener = new MessageListener(this.botChanURL, platform);
        initMessageMatchers();
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.GENERATE_PLATFORM_OTP, this::generatePlatformOTP);
        messageMatchers.put(ChatMatchers.ALLOW_PLATFORM_OTP, this::allowPlatformOTP);
        messageMatchers.put(ChatMatchers.LINK_PLATFORM, this::linkPlatform);
        messageMatchers.put(ChatMatchers.KYS, this::kys);
    }

    private void generatePlatformOTP(Message message) {
        Service.create(botChanURL).get(String.format("/platformOTP?sender=%s&platform=%s", message.getSender(), message.getPlatform()), String.class, new ServiceCallback<String>() {
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

    private void allowPlatformOTP(Message message) {
        String messageText = removeMatcherText(message.getMessage(), ChatMatchers.ALLOW_PLATFORM_OTP).trim();

        Service.create(botChanURL).get(String.format("/userOTP?sender=%s&platform=%s&platformOTP=%s", message.getSender(), message.getPlatform(), messageText), String.class, new ServiceCallback<String>() {
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

    private void linkPlatform(Message message) {
        String messageText = removeMatcherText(message.getMessage(), ChatMatchers.LINK_PLATFORM).trim();

        if (!messageText.isEmpty()) {
            Service.create(botChanURL).get(String.format("/linkPlatform?&sender=%s&platform=%s&userOTP=%s", message.getSender(), message.getPlatform(), messageText), String.class, new ServiceCallback<String>() {
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

    private void kys(Message message) {
        if (message.isDirect()) {
            MessageUtil.reply(botChanURL, message, ":(");
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMessages() {
        messageListener.checkMessages("/messages", new MatcherList(platform, new ArrayList<>(messageMatchers.keySet())), this::parseMessage);
    }

    private void parseMessage(Message message) {
        messageMatchers.keySet().stream()
                .filter(message.getMessage()::matches)
                .map(messageMatchers::get)
                .forEach(messageConsumer -> messageConsumer.accept(message));
    }
}
