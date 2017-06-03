package com.netply.zero.${package};

import com.netply.zero.service.base.BasicLoginCallback;
import com.netply.zero.service.base.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;

@Component
public class ${classPrefix}LoginAndMessageMatcherRegistrationBean {
    private String botChanURL;

    @Autowired
    public ${classPrefix}LoginAndMessageMatcherRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
    }

    @Async
    @PostConstruct
    public void loginToBotChan() {
        Service.create(botChanURL).login("test_user3", "$2y$10$PAlJzaGG0pdCJWz6f/W8FOHubkFEld3uwYJeYlHHxx.u7Rxl/4zFS", new BasicLoginCallback());
    }
}
