package com.netply.zero.eventador.league.chat;

import com.netply.botchan.web.model.FromUserMessage;
import com.netply.botchan.web.model.Reply;
import com.netply.botchan.web.model.TrackPlayerRequest;
import com.netply.zero.eventador.league.games.CurrentGameManager;
import com.netply.zero.eventador.league.games.League;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Pair;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LeagueMessageBean {
    private String botChanURL;
    private MessageListener messageListener;


    @Autowired
    public LeagueMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForLeagueMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(ChatMatchers.LEAGUE_MATCHER);
        messageMatchers.add(ChatMatchers.WHO_IS_PLAYING_MATCHER);
        messageMatchers.add(ChatMatchers.TRACK_PLAYER_MATCHER);
        messageMatchers.add(ChatMatchers.UNTRACK_PLAYER_MATCHER);
        messageMatchers.add(ChatMatchers.CHECK_ELO_MATCHER);
        messageMatchers.add(ChatMatchers.WHO_AM_I_TRACKING_MATCHER);
        messageListener.checkMessages("/messages", messageMatchers, this::parseMessage);
    }

    private void parseMessage(FromUserMessage message) {
        String messageText = message.getMessage();
        if (messageText.matches(ChatMatchers.LEAGUE_MATCHER)) {
            Service.create(botChanURL).put("/reply", new Reply(message.getId(), "Sure!"));
        } else if (messageText.matches(ChatMatchers.WHO_IS_PLAYING_MATCHER)) {
            CurrentGameManager.sendCurrentGamesForTrackedPlayers(botChanURL, message);
        } else if (messageText.matches(ChatMatchers.TRACK_PLAYER_MATCHER)) {
            trackPlayer(message);
        } else if (messageText.matches(ChatMatchers.UNTRACK_PLAYER_MATCHER)) {
            unTrackPlayer(message);
        } else if (messageText.matches(ChatMatchers.CHECK_ELO_MATCHER)) {
            checkElo(message);
        } else if (messageText.matches(ChatMatchers.WHO_AM_I_TRACKING_MATCHER)) {
            sendTrackedPlayersList(message);
        }
    }

    private void trackPlayer(final FromUserMessage message) {
        String player = message.getMessage().replaceAll(ChatMatchers.TRACK_PLAYER_MATCHER.replace(" (.*)", ""), "").trim();
        TrackPlayerRequest trackPlayerRequest = new TrackPlayerRequest(null, player);

        Service.create(botChanURL).put(String.format("/trackedPlayer?platformID=%s", message.getPlatformID()), trackPlayerRequest, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {
                MessageUtil.reply(botChanURL, message, "An unknown error has occurred. I was unable to track that player");
            }

            @Override
            public void onSuccess(String output) {
                MessageUtil.reply(botChanURL, message, String.format("%s has been added to your tracked players", player));
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    private void unTrackPlayer(final FromUserMessage message) {
        String player = message.getMessage().replaceAll(ChatMatchers.UNTRACK_PLAYER_MATCHER.replace(" (.*)", ""), "").trim();
        TrackPlayerRequest trackPlayerRequest = new TrackPlayerRequest(null, player);

        Service.create(botChanURL).delete(String.format("/trackedPlayer?platformID=%s", message.getPlatformID()), trackPlayerRequest, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {
                MessageUtil.reply(botChanURL, message, "An unknown error has occurred. I was unable to untrack that player");
            }

            @Override
            public void onSuccess(String output) {
                MessageUtil.reply(botChanURL, message, String.format("%s has been removed from your tracked players", player));
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    private void checkElo(FromUserMessage message) {
        String player = message.getMessage().replaceAll(ChatMatchers.CHECK_ELO_MATCHER.replace(" (.*)", ""), "").trim();
        String rankedStats = "";
        for (Pair<String, String> tierQueueTypeImmutablePair : League.getPlayerElo(player)) {
            rankedStats += tierQueueTypeImmutablePair.getLeft() + " - " + tierQueueTypeImmutablePair.getRight() + "\n";
        }
        MessageUtil.reply(botChanURL, message, player + " has the following ranked stats:" + "\n" + rankedStats.trim());
    }

    private void sendTrackedPlayersList(FromUserMessage message) {
        Service.create(botChanURL).post("/trackedPlayers?platformID=%s", message.getPlatformID(), null, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {
                System.out.println(response.toString());
                MessageUtil.reply(botChanURL, message, "Error retrieving your tracked players");
            }

            @Override
            public void onSuccess(String output) {
                List<String> trackedPlayers = ListUtil.stringToArray(output, String[].class);

                if (trackedPlayers.isEmpty()) {
                    MessageUtil.reply(botChanURL, message, "You are not tracking any players");
                } else {
                    String trackedPlayersString = "";
                    for (String trackedPlayer : trackedPlayers) {
                        trackedPlayersString += trackedPlayer + "\n";
                    }
                    MessageUtil.reply(botChanURL, message, "You are tracking the following players: \n" + trackedPlayersString.trim());
                }
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
