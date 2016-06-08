package com.netply.zero.league;

import java.util.concurrent.CountDownLatch;

public class LeagueGameManager {
    private static LeagueGameManager instance;


    public static LeagueGameManager getInstance() {
        if (instance == null) {
            instance = new LeagueGameManager();
        }
        return instance;
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
