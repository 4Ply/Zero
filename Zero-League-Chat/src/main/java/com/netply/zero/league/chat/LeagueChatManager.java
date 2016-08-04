package com.netply.zero.league.chat;

import com.netply.zero.league.chat.persistence.LeagueChatDatabase;
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LeagueChatManager {
    @Autowired
    LeagueChatDatabase leagueChatDatabase;

    @Value("${key.external.riot.api.key}")
    private String riotAPIKey;


    public LeagueChatManager() {
        RiotAPI.setRegion(Region.EUW);
        RiotAPI.setAPIKey(riotAPIKey);
    }

    @Scheduled(fixedDelay = 5000)
    public void parseIncomingMessages() throws InterruptedException {
        if (leagueChatDatabase != null) {
            leagueChatDatabase.getUnprocessedMessages().forEach(message -> {
                System.out.println(message);
                leagueChatDatabase.processMessage(message.getId());
            });
        }
    }
}
