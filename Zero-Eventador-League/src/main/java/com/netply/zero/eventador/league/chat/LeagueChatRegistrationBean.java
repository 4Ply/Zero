package com.netply.zero.eventador.league.chat;

import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.botchan.web.model.MatcherList;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.SessionManager;
import com.netply.zero.service.base.ZeroCredentials;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import java.util.ArrayList;

@Component
public class LeagueChatRegistrationBean {
    private String botChanUrl;

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals("127.0.0.1"));
    }

    @Autowired
    public LeagueChatRegistrationBean(@Value("${key.server.bot-chan.url}") String botChanUrl) {
        this.botChanUrl = botChanUrl;
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

            @Override
            public String getBotType() {
                return "Zero-Eventador-League";
            }
        };

        Service.create(botChanUrl).login(credentials.getUsername(), credentials.getPasswordHash(), getServiceCallback(credentials));
    }

    private ServiceCallback<BasicResultResponse> getServiceCallback(final ZeroCredentials credentials) {
        return new ServiceCallback<BasicResultResponse>() {
            @Override
            public void onError(ClientResponse response) {
                System.err.println(response.toString());
                throw new RuntimeException("Unable to authenticate with Bot-chan! Error code: " + response.getStatus());
            }

            @Override
            public void onSuccess(String output) {

            }

            @Override
            public void onSuccess(BasicResultResponse parsedResponse) {
                SessionManager.setSessionKey(parsedResponse.getSessionKey());
                registerMessageMatchers(credentials, parsedResponse.getClientID());
            }
        };
    }

    private void registerMessageMatchers(ZeroCredentials credentials, Integer clientID) {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add("(.*)League(.*)");
        Service.create(botChanUrl).put("/messageMatchers", credentials, new MatcherList(clientID, messageMatchers), new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                System.out.print("Registered matchers: ");
                System.out.println(messageMatchers);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
