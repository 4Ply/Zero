package com.netply.zero.eventador.league.chat;

import com.netply.botchan.web.model.*;
import com.netply.zero.eventador.league.games.CurrentGameManager;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class LeagueMessageBean {
    private String botChanURL;
    private String platform;
    private MessageListener messageListener;


    @Autowired
    public LeagueMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.platform}") String platform) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        messageListener = new MessageListener(this.botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForLeagueMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add("(.*)League(.*)");
        messageMatchers.add(ChatMatchers.WHO_IS_PLAYING_MATCHER);
        messageMatchers.add(ChatMatchers.TRACK_PLAYER_MATCHER);
        messageListener.checkMessages("/messages", new MatcherList(SessionManager.getClientID(), messageMatchers), this::parseMessage);
    }

    private void parseMessage(Message message) {
        String lowerCaseMessage = message.getMessage().toLowerCase();
        if (lowerCaseMessage.equalsIgnoreCase("League?")) {
            Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getPlatform(), message.getSender(), "Sure!"));
        } else if (lowerCaseMessage.matches(ChatMatchers.WHO_IS_PLAYING_MATCHER)) {
            CurrentGameManager.sendCurrentGamesForTrackedPlayers(botChanURL, message, platform);
        } else if (lowerCaseMessage.matches(ChatMatchers.TRACK_PLAYER_MATCHER)) {
            trackPlayer(message);
        }
    }

    private void trackPlayer(final Message message) {
        String player = message.getMessage().replaceAll(ChatMatchers.TRACK_PLAYER_MATCHER.replace(" (.*)", ""), "").trim();
        TrackPlayerRequest trackPlayerRequest = new TrackPlayerRequest(new User(String.valueOf(message.getSender()), platform), player);

        Service.create(botChanURL).put("/trackedPlayer", new BasicSessionCredentials(), trackPlayerRequest, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {
                MessageUtil.reply(botChanURL, message, "An unknown error has occurred. I was unable to track that player");
            }

            @Override
            public void onSuccess(String output) {
                MessageUtil.reply(botChanURL, message, String.format("%s added to your tracked players", player));
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
