package com.netply.zero.chatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.zero.chatter"
})
public class ChatterAppRunner {
    public static void main(String[] args) {
        Logger.getGlobal().setLevel(Level.ALL);
        SpringApplication.run(ChatterAppRunner.class, args);
    }
}
