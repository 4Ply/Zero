package com.netply.zero.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulerAppConfig {
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

    @Value("${key.server.bot-chan.url}")
    private String botChanURL;


    @Bean
    public Scheduler scheduler() throws SchedulerException {
        return new StdSchedulerFactory().getScheduler();
    }
}
