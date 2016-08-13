package com.netply.zero.discord;

import com.netply.zero.discord.chat.DiscordChatManager;
import com.netply.zero.discord.chat.DiscordChatManagerImpl;
import com.netply.zero.discord.persistence.Database;
import com.netply.zero.discord.persistence.DiscordChatDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import sx.blah.discord.util.DiscordException;

import java.sql.SQLException;

@Configuration
@EnableAsync
@EnableScheduling
public class DiscordChatAppConfig {
    @Value("${key.database.mysql.ip}")
    private String mysqlIp;

    @Value("${key.database.mysql.port}")
    private int mysqlPort;

    @Value("${key.database.mysql.db}")
    private String mysqlDb;

    @Value("${key.database.mysql.user}")
    private String mysqlUser;

    @Value("${key.database.mysql.password}")
    private String mysqlPassword;

    @Value("${key.external.discord.api.key}")
    private String discordAPIKey;

    @Value("${key.server.bot-chan.url}")
    private String botChanURL;

    @Value("${key.platform}")
    private String platform;


    @Bean
    public DiscordChatDatabase leagueChatDatabase() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        return new Database(mysqlIp, mysqlPort, mysqlDb, mysqlUser, mysqlPassword);
    }

    @Bean
    public DiscordChatManager discordChatManager() throws DiscordException {
        return new DiscordChatManagerImpl(discordAPIKey, botChanURL, platform);
    }
}
