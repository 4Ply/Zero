package com.netply.zero.eventmock.chat;

public class ChatMatchers {
    public static final String MOCK_EVENT_MATCHER = "(?i)(.*)@!mockEvent=(.*)";

    static {
        "".matches(MOCK_EVENT_MATCHER);
    }
}
