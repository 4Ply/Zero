package com.netply.zero.eventador.league.games;

import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.User;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import com.robrua.orianna.type.core.currentgame.Participant;
import com.sun.jersey.api.client.ClientResponse;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CurrentGameManager {
    public static void sendCurrentGamesForTrackedPlayers(final String botChanURL, final Message message, String platform) {
        Service.create(botChanURL).post("/trackedPlayers", new BasicSessionCredentials(), new User(message.getSender(), platform), null, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {
                System.out.println(response.toString());
                MessageUtil.reply(botChanURL, message, "Error retrieving your tracked players");
            }

            @Override
            public void onSuccess(String output) {
                List<String> trackedPlayers = ListUtil.stringToArray(output, String[].class);
                parseCurrentGamesForTrackedPlayers(botChanURL, trackedPlayers, message);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    public static void parseCurrentGamesForTrackedPlayers(String botChanURL, List<String> trackedPlayers, Message message) {
        Consumer<HashMap<String, CurrentGame>> inGameSummonerListConsumer = currentGameHashMap -> {
            String playersPlaying = currentGameHashMap.entrySet().stream().filter(currentGameEntry -> currentGameEntry.getValue() != null).map(currentGameEntry -> {
                String summonerName = currentGameEntry.getKey();
                CurrentGame game = currentGameEntry.getValue();

                Participant participant = getParticipant(summonerName, game);
                if (participant != null) {
                    String mapName = game.getMap() != null ? game.getMap().name() : "UNKNOWN_MAP";
                    String queueType = game.getQueueType() != null ? game.getQueueType().name() : "UNKNOWN_MODE";
                    return String.format("\n%s (%s) - %s/%s", summonerName, participant.getChampion().getName(), mapName, queueType);
                } else {
                    return String.format("\n%s", summonerName);
                }
            }).collect(Collectors.joining("\n"));

            String response;
            if (playersPlaying.isEmpty()) {
                response = "There are currently no players in game.";
            } else {
                response = "Here are the players currently in game: \n" + playersPlaying;
            }
            MessageUtil.reply(botChanURL, message, response);
        };

        HashMap<String, CurrentGame> currentGameHashMap = new HashMap<>();
        // TODO: 2016/10/01 Use DB to store this information as getting it from League.API will impact performance
        trackedPlayers.forEach(s -> currentGameHashMap.put(s, League.getCurrentGame(s)));
        inGameSummonerListConsumer.accept(currentGameHashMap);
    }

    private static Participant getParticipant(String summonerName, CurrentGame game) {
        for (Participant participant : game.getParticipants()) {
            if (participant.getSummonerName().equalsIgnoreCase(summonerName)) {
                return participant;
            }
        }
        return null;
    }
}
