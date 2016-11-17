package com.netply.zero.scheduler.chat;

public class EventMatchers {
    public static final String REMIND_ME_MATCHER = "(?i)(.*)Remind me at(.*)";
    public static final String DOWNLOAD_MUSIC_MATCHER = "(?i)(.*)Download song(.*)";
    public static final String DOWNLOAD_AND_PLAY_MUSIC_MATCHER = "(?i)(.*)Download and play(.*)";

    static {
        "".matches(REMIND_ME_MATCHER);
        "".matches(DOWNLOAD_MUSIC_MATCHER);
        "".matches(DOWNLOAD_AND_PLAY_MUSIC_MATCHER);
    }
}
