package com.netply.zero.music;

import com.netply.zero.music.status.StatusUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.zero.music.chat",
        "com.netply.zero.music"
})
public class ZeroMusicRunner {
    public static void main(String[] args) {
        Logger.getGlobal().setLevel(Level.ALL);
        StatusUtil.setInitDate(new Date());
        SpringApplication.run(ZeroMusicRunner.class, args);
    }
}
