package com.netply.zero.music;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class MusicAppConfig {
    @Value("${key.server.bot-chan.url}")
    private String botChanURL;
}
