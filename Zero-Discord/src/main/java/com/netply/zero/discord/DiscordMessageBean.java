package com.netply.zero.discord;

import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.discord.chat.DiscordChatManager;
import com.netply.zero.service.base.BasicLoginCallback;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.credentials.ZeroCredentials;
import com.netply.zero.service.base.messaging.MessageListener;
import com.sun.jersey.api.client.ClientResponse;
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
    private String platform;
    private MessageListener messageListener;
    private DiscordChatManager discordChatManager;


    @Autowired
    public DiscordMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL,
                              @Value("${key.platform}") String platform, DiscordChatManager discordChatManager) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(botChanURL);
        this.platform = platform;
        this.discordChatManager = discordChatManager;
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

        Service.create(botChanURL).login(credentials.getUsername(), credentials.getPasswordHash(), getServiceCallback(credentials));
    }

    private ServiceCallback<BasicResultResponse> getServiceCallback(final ZeroCredentials credentials) {
        BasicLoginCallback basicLoginCallback = new BasicLoginCallback();
        return new ServiceCallback<BasicResultResponse>() {
            @Override
            public void onError(ClientResponse response) {
                basicLoginCallback.onError(response);
            }

            @Override
            public void onSuccess(String output) {
                basicLoginCallback.onSuccess(output);
            }

            @Override
            public void onSuccess(BasicResultResponse parsedResponse) {
                basicLoginCallback.onSuccess(parsedResponse);
                registerPlatformReplyMatchers(credentials, parsedResponse.getClientID());
            }
        };
    }

    private void registerPlatformReplyMatchers(ZeroCredentials credentials, Integer clientID) {
        ArrayList<String> platformMatchers = new ArrayList<>();
        platformMatchers.add(platform);
        Service.create(botChanURL).put("/platformMatchers", credentials, new MatcherList(clientID, platformMatchers), new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                System.out.print("Registered platform matchers: ");
                System.out.println(platformMatchers);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void registerPlatformReplyMatchers() {
        registerPlatformReplyMatchers(new BasicSessionCredentials(), SessionManager.getClientID());
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForDiscordReplies() {
        String url = String.format("/replies?id=%s", String.valueOf(SessionManager.getClientID()));
        messageListener.checkReplies(url, this::parseReply);
    }

    private void parseReply(Reply reply) {
        discordChatManager.sendMessage(reply.getTarget(), reply.getMessage());
    }
}
