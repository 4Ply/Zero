package com.netply.zero.eventador.league.chat;

import com.netply.botchan.web.model.BasicMessageObject;
import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
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

@Component
public class LeagueMessageBean {
    private String botChanURL;
    private MessageListener messageListener;


    @Autowired
    public LeagueMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(this.botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForLeagueMessages() {
        String url = String.format("/messages?id=%s", String.valueOf(SessionManager.getClientID()));
        messageListener.checkMessages(url, this::parseMessage);
    }

    private void parseMessage(Message message) {
        String lowerCaseMessage = message.getMessage().toLowerCase();
        if (lowerCaseMessage.equalsIgnoreCase("League?")) {
            Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getPlatform(), message.getSender(), "Sure!"));
        } else if (lowerCaseMessage.matches(ChatMatchers.WHO_IS_PLAYING_MATCHER)) {
            CurrentGameManager.sendCurrentGamesForTrackedPlayers(message, botChanURL);
        } else if (lowerCaseMessage.matches(ChatMatchers.TRACK_PLAYER_MATCHER)) {
            trackPlayer(message);
        }
    }

    private void trackPlayer(final Message message) {
        String player = message.getMessage().replaceAll(ChatMatchers.TRACK_PLAYER_MATCHER.replace(" (.*)", ""), "").trim();
        String url = String.format("/trackedPlayer?id=%s", String.valueOf(message.getSender()));
        Service.create(botChanURL).put(url, new BasicSessionCredentials(), new BasicMessageObject(0, player), new ServiceCallback<Object>() {
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
