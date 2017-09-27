package com.netply.zero.eventador.league.chat;

import com.netply.zero.service.base.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LeagueChatLoginAndMessageMatcherRegistrationBean {
    private String botChanURL;
    private String authenticatenToken;


    @Autowired
    public LeagueChatLoginAndMessageMatcherRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.node.auth.token}") String authenticatenToken) {
        this.botChanURL = botChanURL;
        this.authenticatenToken = authenticatenToken;
    }

    @Async
    @PostConstruct
    public void registerToBotChan() {
        Service.create(botChanURL).setAuthenticateToken(authenticatenToken);
    }
}
