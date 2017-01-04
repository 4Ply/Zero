package com.netply.zero.music.chat;

public class ChatMatchers {
    public static final String PLAY_MUSIC_MATCHER = "(?i)(.*)Play song(.*)";
    public static final String DOWNLOAD_MUSIC_MATCHER = "(?i)(.*)Download song(.*)";
    public static final String DOWNLOAD_AND_PLAY_MUSIC_MATCHER = "(?i)(.*)Download and play(.*)";
    public static final String STOP_PLAYING = "(?i)(.*)Stop (playing|playback)(.*)";

    static {
        "".matches(PLAY_MUSIC_MATCHER);
        "".matches(DOWNLOAD_MUSIC_MATCHER);
        "".matches(DOWNLOAD_AND_PLAY_MUSIC_MATCHER);
        "".matches(STOP_PLAYING);
    }
}
