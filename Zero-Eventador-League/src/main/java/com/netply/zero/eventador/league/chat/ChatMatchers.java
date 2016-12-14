package com.netply.zero.eventador.league.chat;

public class ChatMatchers {
    public static final String LEAGUE_MATCHER = "(?i)(.*)League(.*)";
    public static final String WHO_IS_PLAYING_MATCHER = "(?i)(who|who's|who is) (playing|in game)(.*)";
    public static final String TRACK_PLAYER_MATCHER = "(?i)track player (.*)";
    public static final String CHECK_ELO_MATCHER = "(?i)what elo is (.*)";
    public static final String WHO_AM_I_TRACKING_MATCHER = "(?i)who am I tracking(.*)";

    static {
        "".matches(LEAGUE_MATCHER);
        "".matches(WHO_IS_PLAYING_MATCHER);
        "".matches(TRACK_PLAYER_MATCHER);
        "".matches(CHECK_ELO_MATCHER);
        "".matches(WHO_AM_I_TRACKING_MATCHER);
    }
}
