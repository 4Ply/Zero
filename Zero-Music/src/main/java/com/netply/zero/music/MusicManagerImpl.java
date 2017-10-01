package com.netply.zero.music;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.netply.zero.music.chat.MusicMessageBean.MUSIC_DIR;

@Component
public class MusicManagerImpl implements MusicManager {
    @Override
    public List<String> musicList() {
        File directory = new File(MUSIC_DIR);
        System.out.println(Arrays.toString(directory.listFiles()));
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"mp3"}, true);
        System.out.println(files);

        return files.stream().map(File::getName).collect(Collectors.toList());
    }
}
