package com.netply.zero.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class MusicController {
    private MusicManager musicManager;


    @Autowired
    public MusicController(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<String> musicList() {
        return musicManager.musicList();
    }
}
