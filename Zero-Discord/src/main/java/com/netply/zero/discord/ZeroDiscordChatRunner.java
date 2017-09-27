package com.netply.zero.discord;

import com.netply.zero.discord.status.StatusUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sx.blah.discord.util.DiscordException;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.zero.discord.chat",
        "com.netply.zero.discord"
})
public class ZeroDiscordChatRunner {
    public static void main(String[] args) throws DiscordException {
        Logger.getGlobal().setLevel(Level.ALL);
        StatusUtil.setInitDate(new Date());
        SpringApplication.run(ZeroDiscordChatRunner.class, args);
    }
}
