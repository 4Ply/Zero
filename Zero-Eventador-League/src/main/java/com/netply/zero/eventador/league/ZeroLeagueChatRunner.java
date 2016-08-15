package com.netply.zero.eventador.league;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.web.security.login.controller",
        "com.netply.zero.eventador.league",
        "com.netply.zero.eventador.league.games"
})
public class ZeroLeagueChatRunner {
    public static void main(String[] args) {
        Logger.getGlobal().setLevel(Level.ALL);
        SpringApplication.run(ZeroLeagueChatRunner.class, args);
    }
}
