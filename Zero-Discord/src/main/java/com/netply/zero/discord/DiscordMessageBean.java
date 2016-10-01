package com.netply.zero.discord;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.discord.chat.DiscordChatManager;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.netply.zero.service.base.BasicLoginCallback;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.credentials.ZeroCredentials;
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
    private DiscordChatManager discordChatManager;
    private TrackedUserManager trackedUserManager;


    @Autowired
    public DiscordMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, DiscordChatManager discordChatManager, TrackedUserManager trackedUserManager) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(botChanURL);
        this.discordChatManager = discordChatManager;
        this.trackedUserManager = trackedUserManager;
    }

    @Async
    @PostConstruct
    public void registerToBotChan() {
        ZeroCredentials credentials = new ZeroCredentials() {
            @Override
            public String getUsername() {
                return "test_user3";
            }

            @Override
            public String getSessionKey() {
                return SessionManager.getSessionKey();
            }

            @Override
            public String getPasswordHash() {
                return "$2y$10$PAlJzaGG0pdCJWz6f/W8FOHubkFEld3uwYJeYlHHxx.u7Rxl/4zFS";
            }
        };

        Service.create(botChanURL).login(credentials.getUsername(), credentials.getPasswordHash(), new BasicLoginCallback());
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForDiscordReplies() {
        ArrayList<String> trackedUsers = new ArrayList<>();
        trackedUsers.addAll(trackedUserManager.getAllTrackedUsers());
        messageListener.checkReplies("/replies", new MatcherList(SessionManager.getClientID(), trackedUsers), this::parseReply);
    }

    private void parseReply(Reply reply) {
        discordChatManager.sendMessage(reply.getTarget(), reply.getMessage());
    }
}
