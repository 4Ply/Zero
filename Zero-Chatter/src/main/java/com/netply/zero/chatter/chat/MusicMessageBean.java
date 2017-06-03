package com.netply.zero.chatter.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class MusicMessageBean {
    private String botChanURL;
    private MessageListener messageListener;
    private Map<String, Consumer<Message>> messageMatchers;


    @Autowired
    public MusicMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(this.botChanURL);
        initMessageMatchers();
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.KYS, this::kys);
    }

    private void kys(Message message) {
        if (message.isDirect()) {
            MessageUtil.reply(botChanURL, message, ":(");
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMusicMessages() {
        messageListener.checkMessages("/messages", new MatcherList(SessionManager.getClientID(), new ArrayList<>(messageMatchers.keySet())), this::parseMessage);
    }

    private void parseMessage(Message message) {
        messageMatchers.keySet().stream()
                .filter(message.getMessage()::matches)
                .map(messageMatchers::get)
                .forEach(messageConsumer -> messageConsumer.accept(message));
    }
}
