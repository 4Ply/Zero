package com.netply.zero.league;

import com.netply.core.running.ProcessRunner;

public class LeagueRunner {
    public static void main(String[] args) {
        League.init();

        ProcessRunner.startParserThread(LeagueGameManager.getInstance()::parseCurrentGamesFromBotChan, 60000);
    }
}
