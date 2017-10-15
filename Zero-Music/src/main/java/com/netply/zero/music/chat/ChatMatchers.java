package com.netply.zero.music.chat;

class ChatMatchers {
    static final String PLAY_MUSIC_MATCHER = "(?i)(.*)Play song(.*)";
    static final String DOWNLOAD_MUSIC_MATCHER = "(?i)(.*)Download song(.*)";
    static final String DOWNLOAD_AND_PLAY_MUSIC_MATCHER = "(?i)(.*)Download and play(.*)";
    static final String DOWNLOAD_AND_PLAY_MUSIC_MATCHER_SHORTCUT = "(?i)(.*)dap (.*)";
    static final String STOP_PLAYING = "(?i)(.*)Stop (playing|playback)(.*)";
    static final String SKIP_SONG = "(?i)(.*)Skip (playing|playback|song|track|this)(.*)";
}
