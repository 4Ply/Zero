package com.netply.zero.music.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.messaging.MessageListener;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Component
public class MusicMessageBean {
    public static final String MUSIC_DIR = "/Music";
    private String botChanURL;
    private String platform;
    private MessageListener messageListener;
    private Process songProcess;


    @Autowired
    public MusicMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.platform}") String platform) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        messageListener = new MessageListener(this.botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMusicMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(ChatMatchers.PLAY_MUSIC_MATCHER);
        messageMatchers.add(ChatMatchers.DOWNLOAD_MUSIC_MATCHER);
        messageMatchers.add(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER);
        messageMatchers.add(ChatMatchers.STOP_PLAYING);
        messageListener.checkMessages("/messages", new MatcherList(SessionManager.getClientID(), messageMatchers), this::parseMessage);
    }

    private void parseMessage(Message message) {
        String messageText = message.getMessage();
        if (messageText.matches(ChatMatchers.PLAY_MUSIC_MATCHER)) {
            String filePath = messageText.replaceAll(ChatMatchers.PLAY_MUSIC_MATCHER.replace("(.*)", "").replace("(.*)", ""), "").trim();
            playSong(message, filePath);
        } else if (messageText.matches(ChatMatchers.DOWNLOAD_MUSIC_MATCHER)) {
            String filePath = messageText.replaceAll(ChatMatchers.DOWNLOAD_MUSIC_MATCHER.replace("(.*)", "").replace("(.*)", ""), "").trim();
            downloadSong(message, filePath);
        } else if (messageText.matches(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER)) {
            String filePath = messageText.replaceAll(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER.replace("(.*)", "").replace("(.*)", ""), "").trim();
            String outputFile = downloadSong(message, filePath);
            if (outputFile != null) {
                playSong(message, outputFile);
            }
        } else if (messageText.matches(ChatMatchers.STOP_PLAYING)) {
            executeCommand(new String[]{"cmus-remote", "-s"});
        }
    }

    private String downloadSong(Message message, String youtubeURL) {
        try {
            Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getSender(), "Downloading " + youtubeURL));

            Process process = Runtime.getRuntime().exec(new String[]{"youtube-dl", "--extract-audio", "--audio-format", "mp3",
                    "-o", MUSIC_DIR + "/%(title)s-%(id)s.%(ext)s", youtubeURL});
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
            System.out.println(output);
            String reply;
            if (output.contains("ERROR")) {
                reply = "Something went wrong. I was unable to download that song";
            } else {
                reply = "Song downloaded! Saved as " + outputFile;
            }
            Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getSender(), reply));
            return outputFile;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void playSong(Message message, String filePath) {
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
        System.out.println(Arrays.toString(directory.listFiles()));
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"mp3"}, true);
        System.out.println(files);
        Optional<File> fileOptional = files.stream().sorted().filter(file -> file.getName().toLowerCase().contains(filePath.toLowerCase())).findFirst();
        String reply;
        if (fileOptional.isPresent()) {
            File file = fileOptional.get();
            reply = "Playing " + file.getName();
            songProcess = executeCommand(new String[]{"cmus-remote", "--server", "192.168.2.37:8545", "--passwd", "so-very-cmus", "-f", file.getAbsolutePath()});
        } else {
            reply = "I can't find any song containing the text \"" + filePath + "\"";
        }
        Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getSender(), reply));

        Thread stopSongProcess = new Thread() {
            public void run() {
                if (songProcess != null) {
                    songProcess.destroy();
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(stopSongProcess);
    }

    private Process executeCommand(String[] command) {
//        StringBuilder output = new StringBuilder();

        try {
            System.out.println("Command: " + Arrays.toString(command));
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

//        System.out.println(output.toString());

        return null;
    }
}
