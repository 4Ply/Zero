package com.netply.zero.eventador.league.games;

import com.netply.botchan.web.model.User;
import com.netply.zero.eventador.league.chat.persistence.LeagueChatDatabase;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import com.robrua.orianna.type.core.game.Game;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LeagueGameStateChangedBean {
    private final String botChanURL;
    private String platform;
    private LeagueChatDatabase leagueChatDatabase;


    @Autowired
    public LeagueGameStateChangedBean(@Value("${key.server.bot-chan.url}") String botChanURL,
                                      @Value("${key.platform}") String platform,
                                      @Value("${key.external.riot.api.key}") String riotAPIKey,
                                      LeagueChatDatabase leagueChatDatabase) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        this.leagueChatDatabase = leagueChatDatabase;
        RiotAPI.setRegion(Region.EUW);
        RiotAPI.setAPIKey(riotAPIKey);
    }

    @Scheduled(fixedDelay = 60000)
    @Async
    public void checkGameStates() {
        Service.create(botChanURL).get("/allTrackedPlayers?platform=LEAGUE", new BasicSessionCredentials(), null, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                List<String> trackedPlayers = ListUtil.stringToArray(output, String[].class).stream().distinct().collect(Collectors.toList());
                updateTrackedPlayerGames(trackedPlayers);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    private void updateTrackedPlayerGames(List<String> trackedPlayers) {
        for (String trackedPlayer : trackedPlayers) {
            long lastKnownGameId = leagueChatDatabase.getCurrentGameId(trackedPlayer);

            CurrentGame currentGame = League.getCurrentGame(trackedPlayer);
            long currentGameID = currentGame == null ? -1 : currentGame.getID();
            leagueChatDatabase.updateGameState(trackedPlayer, currentGameID);

            if (lastKnownGameId != currentGameID) {
                handleGameStateChanged(trackedPlayer, lastKnownGameId, currentGame);
            }
        }
    }

    private void handleGameStateChanged(String trackedPlayer, long lastKnownGameId, CurrentGame currentGame) {
        Optional<Game> gameOptional = RiotAPI.getRecentGames(trackedPlayer).stream().filter(game -> game.getID() == lastKnownGameId).findFirst();
        if (gameOptional.isPresent()) {
            Game game = gameOptional.get();
            String cleanTrackedPlayer = trackedPlayer;
            try {
                cleanTrackedPlayer = URLEncoder.encode(trackedPlayer, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = String.format("/trackers?player=%s&platform=%s", cleanTrackedPlayer, platform);
            Service.create(botChanURL).get(url, new BasicSessionCredentials(), null, new ServiceCallback<Object>() {
                @Override
                public void onError(ClientResponse response) {

                }

                @Override
                public void onSuccess(String output) {
                    List<User> users = ListUtil.stringToArray(output, User[].class);
                    boolean didWin = game.getStats().getWin();
                    String replyMessage = String.format("%s just finished a game! (and %s) - they placed %s wards, and killed %s wards",
                            trackedPlayer, didWin ? "WON" : "lost", game.getStats().getWardsPlaced(), game.getStats().getWardsKilled());
                    for (User user : users) {
//                        Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(user.getClientID(), replyMessage));
                    }
                }

                @Override
                public void onSuccess(Object parsedResponse) {

                }
            });
        }
    }
}
