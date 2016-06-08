package com.netply.zero.league;

import com.netply.core.logging.Log;
import com.robrua.orianna.type.core.match.Match;
import com.robrua.orianna.type.core.match.Participant;
import com.robrua.orianna.type.core.match.ParticipantStats;
import com.robrua.orianna.type.core.summoner.Summoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class CompletedGameProcessor {
    private static final Logger log = Log.getLogger();
    private HashMap<String, HashMap<Long, ArrayList<String>>> omg = new HashMap<>();
    private HashMap<Long, Match> gameHashMap = new HashMap<>();


    public void processCompletedGames(HashMap<String, Long> unProcessedGames) {
        for (String summonerName : unProcessedGames.keySet()) {
            Long gameId = unProcessedGames.get(summonerName);
            League.getInstance().addToQueue(() -> League.getCurrentGame(summonerName), currentGame -> {
                if (currentGame == null || currentGame.getID() != gameId) {
                    processFinishedGame(summonerName, gameId);
                }
            });
        }
    }

    private void processFinishedGame(String summonerName, Long gameId) {
        CountDownLatch countDownLatch2 = new CountDownLatch(1);

        Database.getInstance().addToQueue(connection -> Database.getTrackersForSummoner(connection, summonerName), trackers -> {
            for (String trackerJID : trackers) {
                addSummoner(trackerJID, gameId, summonerName);
            }
            countDownLatch2.countDown();
        });

        try {
            countDownLatch2.await();
        } catch (InterruptedException e) {
            log.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSummoner(String trackerJID, Long gameId, String summonerName) {
        if (!omg.containsKey(trackerJID)) {
            omg.put(trackerJID, new HashMap<>());
        }
        if (!omg.get(trackerJID).containsKey(gameId)) {
            omg.get(trackerJID).put(gameId, new ArrayList<>());
        }

        omg.get(trackerJID).get(gameId).add(summonerName);
    }

    public void parseGamesForEachTracker() {
        for (String trackerJID : omg.keySet()) {
            HashMap<Long, ArrayList<String>> gameSummonerNameMap = omg.get(trackerJID);
            for (Long gameId : gameSummonerNameMap.keySet()) {
                parseGamesForEachTracker(trackerJID, gameSummonerNameMap, gameId);
            }
        }
    }

    private void parseGamesForEachTracker(String trackerJID, HashMap<Long, ArrayList<String>> gameSummonerNameMap, Long gameId) {
        String names = "";
        HashMap<String, PlayerStat> statMap = new HashMap<>();
        for (String summonerName : gameSummonerNameMap.get(gameId)) {
            if (!gameHashMap.containsKey(gameId)) {
                gameHashMap.put(gameId, League.getGameFromSummonerHistory(gameId));
            }
            statMap.put(summonerName, getStats(summonerName, gameId));
            names += names.isEmpty() ? "" : ", " + summonerName;
        }

        int size = gameSummonerNameMap.get(gameId).size();
        if (size == 0) {
            log.warning("CompletedGameProcessor::parseGamesForEachTracker number of summoners in game = 0");
            return;
        }
        if (size == 1) {
            PlayerStat playerStat = statMap.get(names);
//            MessageQueue.getInstance().add(new SendDiscordMessage(trackerJID, names + " has just finished a game on " + playerStat.champion + ", going " + playerStat.kda));
        } else {
//            MessageQueue.getInstance().add(new SendDiscordMessage(trackerJID, names + " have just finished a game!"));
        }
    }

    private PlayerStat getStats(String summonerName, Long gameId) {
        Match match = gameHashMap.get(gameId);
        List<Participant> participants = match.getParticipants();
        for (Participant participant : participants) {
            Summoner summoner = participant.getSummoner();
            if (summoner.getName().equalsIgnoreCase(summonerName)) {
                ParticipantStats stats = participant.getStats();
                String kda = String.format("%s/%s/%s", stats.getKills(), stats.getDeaths(), stats.getAssists());
                return new PlayerStat(summonerName, participant.getChampion().getName(), kda, stats.getPentaKills());
            }
        }
        return new PlayerStat(summonerName);
    }

    private static class PlayerStat {
        public String summonerName;
        public String champion;
        public String kda;
        public long pentaKills = 0;


        public PlayerStat(String summonerName) {
            this.summonerName = summonerName;
        }

        public PlayerStat(String summonerName, String champion, String kda, long pentaKills) {
            this.summonerName = summonerName;
            this.champion = champion;
            this.kda = kda;
            this.pentaKills = pentaKills;
        }
    }
}
