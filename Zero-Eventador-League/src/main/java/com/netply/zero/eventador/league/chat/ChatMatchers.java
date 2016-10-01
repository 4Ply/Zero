package com.netply.zero.eventador.league.chat;

public class ChatMatchers {
    public static final String LEAGUE_MATCHER = "(?i)(.*)League(.*)";
    public static final String WHO_IS_PLAYING_MATCHER = "(?i)(who|who's|who is) (playing|in game)(.*)";
    public static final String TRACK_PLAYER_MATCHER = "(?i)track player (.*)";

    static {
        "".matches(LEAGUE_MATCHER);
        "".matches(WHO_IS_PLAYING_MATCHER);
        "".matches(TRACK_PLAYER_MATCHER);
    }
}
