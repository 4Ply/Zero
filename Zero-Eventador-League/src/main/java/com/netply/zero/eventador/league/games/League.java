package com.netply.zero.eventador.league.games;

import com.netply.zero.service.base.Pair;
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import com.robrua.orianna.type.core.summoner.Summoner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class League {
    public static CurrentGame getCurrentGame(String summonerName) {
        CurrentGame currentGame = null;
        try {
            currentGame = RiotAPI.getCurrentGame(summonerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentGame;
    }

    public static List<Pair<String, String>> getPlayerElo(String summonerName) {
        try {
            Summoner summonerByName = RiotAPI.getSummonerByName(summonerName);
            return summonerByName.getLeagues().stream()
                    .map(league -> new Pair<>(league.getQueueType().toString(), league.getTier().toString()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
