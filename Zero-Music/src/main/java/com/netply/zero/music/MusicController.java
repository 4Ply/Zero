package com.netply.zero.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class MusicController {
    private MusicManager musicManager;
    private List<String> cachedMusicList = null;
    private long lastUpdated = System.currentTimeMillis();


    @Autowired
    public MusicController(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody
    List<String> musicList() {
        checkCacheValidity();
        if (cachedMusicList == null) {
            cachedMusicList = musicManager.musicList();
            lastUpdated = System.currentTimeMillis();
        }
        return cachedMusicList;
    }

    private void checkCacheValidity() {
        if (lastUpdated < System.currentTimeMillis() - (1000 * 60 * 15)) {
            cachedMusicList = null;
        }
    }
}
