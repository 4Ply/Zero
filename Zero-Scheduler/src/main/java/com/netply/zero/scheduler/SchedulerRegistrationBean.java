package com.netply.zero.scheduler;

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
public class SchedulerRegistrationBean {
    private final String botChanURL;


    @Autowired
    public SchedulerRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
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
}
