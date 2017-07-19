package com.netply.zero.eventador.league.chat.persistence;

public interface LeagueChatDatabase {
    int updateGameState(String summonerName, long gameID);

    long getCurrentGameId(String summonerName);
}
