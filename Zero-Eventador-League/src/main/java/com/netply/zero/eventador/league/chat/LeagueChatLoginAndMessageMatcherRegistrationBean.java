package com.netply.zero.eventador.league.chat;

import com.netply.zero.service.base.BasicLoginCallback;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.credentials.ZeroCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LeagueChatLoginAndMessageMatcherRegistrationBean {
    private String botChanURL;


    @Autowired
    public LeagueChatLoginAndMessageMatcherRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
    }

    @Async
    @PostConstruct
    public void registerToBotChan() {
        ZeroCredentials credentials = new ZeroCredentials() {
            @Override
            public String getUsername() {
                return "league_bot1";
            }

            @Override
            public String getSessionKey() {
                return SessionManager.getSessionKey();
            }

            @Override
            public String getPasswordHash() {
                return "M4on0LdBxNNuCrwTJXHG6OYvbNUH1JZFOJA9hJ6O4tejT8qXlFIXBewXLdYFaFtLCxc0f4BIFpwb28Z94NPEt5rsVkHagGdglfKO";
            }
        };

        Service.create(botChanURL).login(credentials.getUsername(), credentials.getPasswordHash(), new BasicLoginCallback());
    }
}
