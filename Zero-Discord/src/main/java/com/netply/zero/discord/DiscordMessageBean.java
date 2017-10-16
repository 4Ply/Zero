package com.netply.zero.discord;

import com.netply.botchan.web.model.ToUserMessage;
import com.netply.zero.discord.chat.DiscordChatManager;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.messaging.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Component
public class DiscordMessageBean {
    private final String botChanURL;
    private MessageListener messageListener;
    private String authenticatenToken;
    private DiscordChatManager discordChatManager;
    private TrackedUserManager trackedUserManager;


    @Autowired
    public DiscordMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL,
                              @Value("${key.node.auth.token}") String authenticatenToken,
                              DiscordChatManager discordChatManager, TrackedUserManager trackedUserManager) {
        this.botChanURL = botChanURL;
        this.authenticatenToken = authenticatenToken;
        this.discordChatManager = discordChatManager;
        this.trackedUserManager = trackedUserManager;
        messageListener = new MessageListener(botChanURL);
    }

    @Async
    @PostConstruct
    public void registerToBotChan() {
        Service.create(botChanURL).setAuthenticateToken(authenticatenToken);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForDiscordReplies() {
        ArrayList<String> trackedUsers = new ArrayList<>(trackedUserManager.getAllTrackedUsers());
        messageListener.checkReplies("/replies", trackedUsers, this::parseReply);
    }

    private void parseReply(ToUserMessage toUserMessage) {
        discordChatManager.sendMessage(toUserMessage.getTarget(), toUserMessage.getMessage());
    }
}
