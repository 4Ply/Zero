package com.netply.zero.music.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.botchan.web.model.User;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Consumer;

@Component
public class MusicMessageBean {
    public static final String MUSIC_DIR = "/home/pawel/Music/";
    private String botChanURL;
    private String platform;
    private MessageListener messageListener;
    private Process songProcess;
    private Map<String, Consumer<Message>> messageMatchers;


    @Autowired
    public MusicMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.platform}") String platform) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        messageListener = new MessageListener(botChanURL, platform);
        initMessageMatchers();
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER, this::downloadAndPlay);
        messageMatchers.put(ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER_SHORTCUT, this::downloadAndPlay);
        messageMatchers.put(ChatMatchers.DOWNLOAD_MUSIC_MATCHER, this::download);
        messageMatchers.put(ChatMatchers.DOWNLOAD_MUSIC_MATCHER, this::download);
        messageMatchers.put(ChatMatchers.PLAY_MUSIC_MATCHER, this::playSong);
        messageMatchers.put(ChatMatchers.STOP_PLAYING, this::stopPlayback);
        messageMatchers.put(ChatMatchers.SKIP_SONG, this::skipSong);
    }

    private void downloadAndPlay(Message message) {
        String messageText = message.getMessage();
        String filePath = removeMatcherText(messageText, ChatMatchers.DOWNLOAD_AND_PLAY_MUSIC_MATCHER);
        String outputFile = downloadSong(message, filePath);
        if (outputFile != null) {
            playSong(message, outputFile);
        }
    }

    private void download(Message message) {
        String messageText = message.getMessage();
        String filePath = removeMatcherText(messageText, ChatMatchers.DOWNLOAD_MUSIC_MATCHER);
        downloadSong(message, filePath);
    }

    private void playSong(Message message) {
        String messageText = message.getMessage();
        String filePath = removeMatcherText(messageText, ChatMatchers.PLAY_MUSIC_MATCHER);
        playSong(message, filePath);
    }

    private void stopPlayback(Message message) {
        String permission;
        try {
            permission = URLEncoder.encode("bot.chan.music.stop", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        Service.create(botChanURL).post(String.format("/hasPermission?permission=%s", permission), new User(message.getSender(), message.getPlatform()), Boolean.class, new ServiceCallback<Boolean>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {

            }

            @Override
            public void onSuccess(Boolean parsedResponse) {
                if (parsedResponse != null && parsedResponse) {
                    executeCmusCommand(new String[]{"-s"});
                    MessageUtil.reply(botChanURL, message, "Playback stopped.");
                }
            }
        });
    }

    private void skipSong(Message message) {
        executeCmusCommand(new String[]{"-n"});
        MessageUtil.reply(botChanURL, message, "Track skipped!");
    }

    private String removeMatcherText(String messageText, String matcher) {
        return messageText.replaceAll(matcher.replace("(.*)", "").replace("(.*)", ""), "").trim();
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMusicMessages() {
        messageListener.checkMessages("/messages", new MatcherList(platform, new ArrayList<>(messageMatchers.keySet())), this::parseMessage);
    }

    private void parseMessage(Message message) {
        messageMatchers.keySet().stream()
                .filter(message.getMessage()::matches)
                .map(messageMatchers::get)
                .forEach(messageConsumer -> messageConsumer.accept(message));
    }

    private String downloadSong(Message message, String youtubeURL) {
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
            System.out.println(output);
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
            songProcess = executeCmusCommand(new String[]{"-f", file.getAbsolutePath()});
        } else {
            reply = "I can't find any song containing the text \"" + filePath + "\"";
        }
        Service.create(botChanURL).put("/reply", new Reply(message.getId(), reply));

        Thread stopSongProcess = new Thread() {
            public void run() {
                if (songProcess != null) {
                    songProcess.destroy();
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(stopSongProcess);
    }

    private Process executeCmusCommand(String[] command) {
        return executeCommand(ArrayUtils.addAll(new String[]{"cmus-remote", "--server", "/sock/cmus-socket"}, command));
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
