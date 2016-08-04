package com.netply.zero.league.chat;

import com.netply.core.running.ProcessRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.netply.web.security.login.controller",
        "com.netply.zero.league.chat"
})
public class ZeroLeagueChatRunner {
    public static void main(String[] args) {
        SpringApplication.run(ZeroLeagueChatRunner.class, args);

        ProcessRunner.startParserThread(LeagueChatManager.getInstance()::parseMessages, 60000);
    }
}
