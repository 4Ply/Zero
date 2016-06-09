package com.netply.zero.league;

import com.netply.core.running.ProcessRunner;

public class LeagueRunner {
    public static void main(String[] args) {
        ProcessRunner.startParserThread(LeagueGameManager.getInstance()::parseCurrentGames, 60000);
    }
}
