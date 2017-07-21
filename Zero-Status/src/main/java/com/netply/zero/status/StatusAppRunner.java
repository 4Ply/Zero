package com.netply.zero.status;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.web.security.login.controller",
        "com.netply.zero.status"
})
public class StatusAppRunner {
    public static void main(String[] args) {
        Logger.getGlobal().setLevel(Level.ALL);
        SpringApplication.run(StatusAppRunner.class, args);
    }
}
