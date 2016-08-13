package com.netply.zero.league.chat;

import com.netply.botchan.web.model.NodeDetails;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ZeroCredentials;
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
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add("(.*)League(.*)");
        Service.create(botChanUrl).put("/node", new ZeroCredentials() {
            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public String getSessionKey() {
                return "0de06073-1777-462d-9f1f-c3e7f96b9b71";
            }

            @Override
            public String getPasswordHash() {
                return null;
            }

            @Override
            public String getBotType() {
                return "Zero-Eventador-League";
            }
        }, new NodeDetails("https://127.0.0.1:42731", messageMatchers));
    }
}
