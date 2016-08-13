package com.netply.zero.league.chat;

import com.netply.zero.league.chat.persistence.LeagueChatDatabase;
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LeagueChatManager {
    private LeagueChatDatabase leagueChatDatabase;


    @Autowired
    public LeagueChatManager(LeagueChatDatabase leagueChatDatabase, @Value("${key.external.riot.api.key}") String riotAPIKey) {
        this.leagueChatDatabase = leagueChatDatabase;
        RiotAPI.setRegion(Region.EUW);
        System.out.println(riotAPIKey);
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

    @Scheduled(fixedDelay = 30000)
    public void getCurrentPlayers() {
        CurrentGame currentGame = RiotAPI.getCurrentGame("");
        if (currentGame != null) {
            System.out.println(currentGame.toString());
        } else {
            System.out.println("Not in game");
        }
    }
}
