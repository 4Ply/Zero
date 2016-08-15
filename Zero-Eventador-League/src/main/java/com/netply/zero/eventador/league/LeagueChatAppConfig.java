package com.netply.zero.eventador.league;

import com.netply.zero.eventador.league.chat.persistence.Database;
import com.netply.zero.eventador.league.chat.persistence.LeagueChatDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.SQLException;

@Configuration
@EnableAsync
@EnableScheduling
public class LeagueChatAppConfig {
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


    @Bean
    public LeagueChatDatabase leagueChatDatabase() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        return new Database(mysqlIp, mysqlPort, mysqlDb, mysqlUser, mysqlPassword);
    }
}
