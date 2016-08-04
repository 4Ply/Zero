package com.netply.zero.league.chat;

public class LeagueChatManager {
    private static LeagueChatManager instance;


    public static LeagueChatManager getInstance() {
        if (instance == null) {
            instance = new LeagueChatManager();
        }
        return instance;
    }

    public void parseMessages() throws InterruptedException {

    }
}
