package com.netply.zero.music.chat;

import com.netply.botchan.web.model.FromUserMessage;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.netply.zero.service.base.permissions.PermissionUtil;
import com.netply.zero.service.base.permissions.PermissionsCallback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

@Component
public class MusicMessageBean {
    private static Logger logger = Logger.getLogger(Service.class);
    public static final String MUSIC_DIR = "/home/pawel/Music/";
    private String botChanURL;
    private MessageListener messageListener;
    private Process songProcess;
    private Map<String, Consumer<FromUserMessage>> messageMatchers;


    @Autowired
    public MusicMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(botChanURL);
        initMessageMatchers();
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER, this::downloadAndPlay);
        messageMatchers.put(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER_SHORTCUT, this::downloadAndPlay);
        messageMatchers.put(ChatMatchers.DOWNLOAD_MUSIC_MATCHER, this::download);
        messageMatchers.put(ChatMatchers.PLAY_MUSIC_MATCHER, this::playSong);
        messageMatchers.put(ChatMatchers.STOP_PLAYING, this::stopPlayback);
        messageMatchers.put(ChatMatchers.SKIP_SONG, this::skipSong);
    }

    private void downloadAndPlay(FromUserMessage message) {
        String messageText = message.getMessage();
        String filePath = removeMatcherText(messageText, ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER);
        String outputFile = downloadSong(message, filePath);
        if (outputFile != null) {
            playSong(message, outputFile);
        }
    }

    private void download(FromUserMessage message) {
        String messageText = message.getMessage();
        String filePath = removeMatcherText(messageText, ChatMatchers.DOWNLOAD_MUSIC_MATCHER);
        downloadSong(message, filePath);
    }

    private void playSong(FromUserMessage message) {
        String messageText = message.getMessage();
        String filePath = removeMatcherText(messageText, ChatMatchers.PLAY_MUSIC_MATCHER);
        playSong(message, filePath);
    }

    private void stopPlayback(FromUserMessage message) {
        PermissionUtil.checkPermission(botChanURL, message, "bot.chan.music.stop", new PermissionsCallback() {
            @Override
            public void permissionGranted(String permission) {
                executeCmusCommand(new String[]{"-s"});
                MessageUtil.reply(botChanURL, message, "Playback stopped.");
            }

            @Override
            public void permissionDenied(String permission) {
                MessageUtil.reply(botChanURL, message, "You don't have permission to do that.");
            }
        });
    }

    private void skipSong(FromUserMessage message) {
        executeCmusCommand(new String[]{"-n"});
        MessageUtil.reply(botChanURL, message, "Track skipped!");
    }

    private String removeMatcherText(String messageText, String matcher) {
        return messageText.replaceAll(matcher.replace("(.*)", "").replace("(.*)", ""), "").trim();
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMusicMessages() {
        messageListener.checkMessages("/messages", new ArrayList<>(messageMatchers.keySet()), this::parseMessage);
    }

    private void parseMessage(FromUserMessage message) {
        messageMatchers.keySet().stream()
                .filter(message.getMessage()::matches)
                .map(messageMatchers::get)
                .forEach(messageConsumer -> messageConsumer.accept(message));
    }

    private String downloadSong(FromUserMessage message, String youtubeURL) {
        try {
            Service.create(botChanURL).put("/reply", new Reply(message.getId(), "Downloading " + youtubeURL));

            Process process = Runtime.getRuntime().exec(new String[]{"youtube-dl", "--extract-audio", "--audio-format", "mp3",
                    "-o", MUSIC_DIR + "%(title)s-%(id)s.%(ext)s", youtubeURL});
            process.waitFor();

            String output = "";
            String outputFile = "unknown";
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output += line + "\n";
                String charSequence = "[ffmpeg] Destination: ";
                String alreadyDownloaded = " has already been downloaded";
                if (line.contains(charSequence)) {
                    outputFile = line.replace(charSequence, "");
                    if (outputFile.contains("/")) {
                        String[] split = outputFile.split("/");
                        outputFile = split[split.length - 1];
                    }
                } else if (line.contains(alreadyDownloaded)) {
                    outputFile = line.replace(alreadyDownloaded, "").replace("[download] ", "");
                    if (outputFile.contains("/")) {
                        String[] split = outputFile.split("/");
                        outputFile = split[split.length - 1].split("\\.")[0];
                    }
                }
            }
            logger.info(output);
            String reply;
            if (output.contains("ERROR")) {
                reply = "Something went wrong. I was unable to download that song";
            } else {
                reply = "Song downloaded! Saved as " + outputFile;
            }
            Service.create(botChanURL).put("/reply", new Reply(message.getId(), reply));
            return outputFile;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void playSong(FromUserMessage message, String filePath) {
        if (songProcess != null) {
//            try {
//                songProcess.getOutputStream().write("q\n".getBytes());
//                songProcess.getOutputStream().flush();
//                songProcess.waitFor();
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
            songProcess.destroy();
        }
//        songProcess = executeCommand(new String[]{"mpsyt", "playurl", filePath});

        File directory = new File(MUSIC_DIR);
//        logger.info(Arrays.toString(directory.listFiles()));
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"mp3"}, true);
//        logger.info(files);
        Optional<File> fileOptional = files.stream().sorted().filter(file -> file.getName().toLowerCase().contains(filePath.toLowerCase())).findFirst();
        String reply;
        if (fileOptional.isPresent()) {
            File file = fileOptional.get();
            reply = "Playing " + file.getName();
            songProcess = executeCmusCommand(new String[]{"-f", file.getAbsolutePath()});
        } else {
            reply = "I can't find any song containing the text \"" + filePath + "\"";
        }
        Service.create(botChanURL).put("/reply", new Reply(message.getId(), reply));

        Thread stopSongProcess = new Thread(() -> {
            if (songProcess != null) {
                songProcess.destroy();
            }
        });

        Runtime.getRuntime().addShutdownHook(stopSongProcess);
    }

    private Process executeCmusCommand(String[] command) {
        return executeCommand(ArrayUtils.addAll(new String[]{"cmus-remote", "--server", "/sock/cmus-socket"}, command));
    }

    private Process executeCommand(String[] command) {
//        StringBuilder output = new StringBuilder();

        try {
            logger.info("Command: " + Arrays.toString(command));
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            return processBuilder.start();
//            p = Runtime.getRuntime().exec(command);
//            p.waitFor();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            AudioStream audioStream = new AudioStream(p.getInputStream());

            // play the audio clip with the audioplayer class
//            AudioPlayer.player.start(audioStream);
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        logger.info(output.toString());

        return null;
    }
}
