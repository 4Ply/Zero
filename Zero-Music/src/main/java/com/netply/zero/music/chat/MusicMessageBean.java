package com.netply.zero.music.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.messaging.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class MusicMessageBean {
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
    public void checkForLeagueMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(ChatMatchers.MUSIC_MATCHER);
        messageListener.checkMessages("/messages", new MatcherList(SessionManager.getClientID(), messageMatchers), this::parseMessage);
    }

    private void parseMessage(Message message) {
        String messageText = message.getMessage();
        if (messageText.matches(ChatMatchers.MUSIC_MATCHER)) {
            Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getSender(), "Sure!"));
            String filePath = messageText.replaceAll(ChatMatchers.MUSIC_MATCHER.replace("(.*)", "").replace("(.*)", ""), "").trim();
            playSong(filePath);
        }
    }

    private void playSong(String filePath) {
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
        songProcess = executeCommand(new String[]{"cmus-remote", "-f", filePath});

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
