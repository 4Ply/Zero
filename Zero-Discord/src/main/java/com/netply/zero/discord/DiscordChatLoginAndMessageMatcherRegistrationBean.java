package com.netply.zero.discord;

import com.netply.zero.service.base.BasicLoginCallback;
import com.netply.zero.service.base.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;

@Component
public class DiscordChatLoginAndMessageMatcherRegistrationBean {
    private String botChanUrl;

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals("127.0.0.1"));
    }

    @Autowired
    public DiscordChatLoginAndMessageMatcherRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanUrl) {
        this.botChanUrl = botChanUrl;
    }

    @Async
    @PostConstruct
    public void loginToBotChan() {
        Service.create(botChanUrl).login("test_user3", "$2y$10$PAlJzaGG0pdCJWz6f/W8FOHubkFEld3uwYJeYlHHxx.u7Rxl/4zFS", new BasicLoginCallback());
    }
}
