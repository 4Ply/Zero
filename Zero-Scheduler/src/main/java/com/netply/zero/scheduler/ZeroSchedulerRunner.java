package com.netply.zero.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sx.blah.discord.util.DiscordException;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.web.security.login.controller",
        "com.netply.zero.scheduler.chat",
        "com.netply.zero.scheduler"
})
public class ZeroSchedulerRunner {
    public static void main(String[] args) throws DiscordException {
        Logger.getGlobal().setLevel(Level.ALL);
        SpringApplication.run(ZeroSchedulerRunner.class, args);
    }
}
