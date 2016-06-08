package com.netply.zero.league;

import com.netply.core.running.ProcessRunner;
import com.robrua.orianna.api.core.MatchAPI;
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import com.robrua.orianna.type.core.match.Match;
import com.robrua.orianna.type.core.summoner.Summoner;

import java.sql.Connection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class League {
    private static League instance;
    private final BlockingDeque<Consumer<Connection>> queue = new LinkedBlockingDeque<>();


    public League() {
        new Thread() {
            @Override
            public void run() {
                RiotAPI.setRegion(Region.EUW);
                RiotAPI.setAPIKey(Credentials.RIOT_API_KEY);
                while (ProcessRunner.run) {
                    consumeQueue();
                }
            }
        }.start();
    }

    public static synchronized League getInstance() {
        if (instance == null) {
            instance = new League();
        }
        return instance;
    }

    public <R> void addToQueue(Supplier<R> function, Consumer<R> consumer) {
        queue.add(connection -> {
            R apply = function.get();
            if (consumer != null) {
                consumer.accept(apply);
            }
        });
    }

    private void consumeQueue() {
        try {
            Consumer<Connection> take = queue.take();
            System.out.println("Executing queue item " + take.toString());
            take.accept(null);
            System.out.println("Execution complete.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Summoner getSummonerByName(String targetSummoner) {
        Summoner summoner = null;
        try {
            summoner = RiotAPI.getSummonerByName(targetSummoner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summoner;
    }

    public static boolean summonerExists(String targetSummoner) {
        return getSummonerByName(targetSummoner) != null;
    }

    public static CurrentGame getCurrentGame(String summonerName) {
        try {
            return RiotAPI.getSummonerByName(summonerName).getCurrentGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Match getGameFromSummonerHistory(Long gameId) {
        return MatchAPI.getMatch(gameId);
    }
}
