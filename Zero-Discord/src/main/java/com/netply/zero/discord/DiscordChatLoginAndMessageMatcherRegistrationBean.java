package com.netply.zero.discord;

import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.credentials.ZeroCredentials;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            @Override
            public String getBotType() {
                return "Zero-Discord";
            }
        };

        Service.create(botChanUrl).login(credentials.getUsername(), credentials.getPasswordHash(), getLoginCallback());
    }

    private ServiceCallback<BasicResultResponse> getLoginCallback() {
        return new ServiceCallback<BasicResultResponse>() {
            @Override
            public void onError(ClientResponse response) {
                Logger.getGlobal().log(Level.SEVERE, response.toString());
                throw new RuntimeException("Unable to authenticate with Bot-chan! Error code: " + response.getStatus());
            }

            @Override
            public void onSuccess(String output) {

            }

            @Override
            public void onSuccess(BasicResultResponse parsedResponse) {
                SessionManager.setSessionKey(parsedResponse.getSessionKey());
            }
        };
    }
}
