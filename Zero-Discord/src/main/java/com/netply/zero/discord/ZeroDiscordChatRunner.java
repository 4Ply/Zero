package com.netply.zero.discord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sx.blah.discord.util.DiscordException;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.web.security.login.controller",
        "com.netply.zero.discord.chat",
        "com.netply.zero.discord"
})
public class ZeroDiscordChatRunner {
    public static void main(String[] args) throws DiscordException {
        Logger.getGlobal().setLevel(Level.ALL);
        SpringApplication.run(ZeroDiscordChatRunner.class, args);
    }
}
