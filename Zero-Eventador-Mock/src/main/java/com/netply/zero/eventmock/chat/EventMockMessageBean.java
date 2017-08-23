package com.netply.zero.eventmock.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.permissions.PermissionUtil;
import com.netply.zero.service.base.permissions.PermissionsCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class EventMockMessageBean {
    private String botChanURL;
    private String platform;
    private MessageListener messageListener;
    private Map<String, Consumer<Message>> messageMatchers;


    @Autowired
    public EventMockMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.platform}") String platform) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        messageListener = new MessageListener(botChanURL, platform);
        initMessageMatchers();
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.MOCK_EVENT_MATCHER, this::mockEvent);
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

    private void mockEvent(Message message) {
        System.out.printf("MOCK EVENT: [%s] : %s%n", message.getSender(), message.getMessage());
        PermissionUtil.checkPermission(botChanURL, message, "bot.chan.event.mock", new PermissionsCallback() {
            @Override
            public void permissionGranted(String permission) {

            }

            @Override
            public void permissionDenied(String permission) {

            }
        });
    }
}
