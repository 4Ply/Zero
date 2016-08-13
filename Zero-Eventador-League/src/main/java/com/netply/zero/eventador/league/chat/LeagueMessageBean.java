package com.netply.zero.eventador.league.chat;

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

@Component
public class LeagueMessageBean {
    private String botChanURL;
    private MessageListener messageListener;


    @Autowired
    public LeagueMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(this.botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForLeagueMessages() {
        String url = String.format("/messages?id=%s", String.valueOf(SessionManager.getClientID()));
        messageListener.checkMessages(url, this::parseMessage);
    }

    private void parseMessage(Message message) {
        if (message.getMessage().equalsIgnoreCase("League?")) {
            Service.create(botChanURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getPlatform(), message.getSender(), "Sure!"));
        }
    }
}
