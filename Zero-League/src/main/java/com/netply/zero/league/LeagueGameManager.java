package com.netply.zero.league;

import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.botchan.web.model.SimpleList;
import com.netply.zero.service.base.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class LeagueGameManager {
    private static LeagueGameManager instance;


    public static LeagueGameManager getInstance() {
        if (instance == null) {
            instance = new LeagueGameManager();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void parseCurrentGamesFromBotChan() throws InterruptedException {
        Service service = Service.create(Credentials.BASE_URL);
//        ArrayList<String> trackedSummoners = service.get("trackedPlayers", new Credentials(), ArrayList.class);
        ArrayList<String> trackedSummoners = new ArrayList<>();

        List<String> playerList = trackedSummoners.stream()
                .filter(playerName -> playerName != null)
                .map(League::getCurrentGame)
                .filter(currentGame -> currentGame != null)
                .map(currentGame -> String.valueOf(currentGame.getParticipants()))
                .collect(Collectors.toList());

        service.post("currentGames", new Credentials(), BasicResultResponse.class, new SimpleList(new ArrayList<>(playerList)));
    }

    public void parseCurrentGames() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Database.getInstance().addToQueue(Database::getAllTrackedLoLSummoners, summoners -> summoners.stream().forEach(summonerName -> {
            League.getInstance().addToQueue(() -> League.getCurrentGame(summonerName), currentGame -> {
                if (currentGame != null) {
                    Database.getInstance().addToQueue(connection -> Database.logCurrentGame(connection, currentGame.getID(), summonerName), null);
                }
                countDownLatch.countDown();
            });
        }));

        countDownLatch.await();
    }

    public void parseCompletedGames() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CompletedGameProcessor completedGameProcessor = new CompletedGameProcessor();

        Database.getInstance().addToQueue(Database::getUnProcessedLoLGames, unProcessedGames -> {
            completedGameProcessor.processCompletedGames(unProcessedGames);

            countDownLatch.countDown();
        });

        countDownLatch.await();

        completedGameProcessor.parseGamesForEachTracker();
    }
}
