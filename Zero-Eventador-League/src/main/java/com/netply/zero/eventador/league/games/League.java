package com.netply.zero.eventador.league.games;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.currentgame.CurrentGame;

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
}
