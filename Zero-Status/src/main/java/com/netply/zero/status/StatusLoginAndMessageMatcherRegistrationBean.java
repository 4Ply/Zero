package com.netply.zero.status;

import com.netply.zero.service.base.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StatusLoginAndMessageMatcherRegistrationBean {
    private String botChanURL;

    @Autowired
    public StatusLoginAndMessageMatcherRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
    }

    @Async
    @PostConstruct
    public void loginToBotChan() {
        Service.create(botChanURL).setAuthenticateToken("test_user3");
    }
}
