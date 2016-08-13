package com.netply.zero.eventador.league.chat;

public class ChatMatchers {
    public static final String WHO_IS_PLAYING_MATCHER = "(who|who's|who is) (playing|in game) (.*)";
    public static final String TRACK_PLAYER_MATCHER = "(watch|track) player (.*)";

    static {
        "".matches(WHO_IS_PLAYING_MATCHER);
        "".matches(TRACK_PLAYER_MATCHER);
    }
}
