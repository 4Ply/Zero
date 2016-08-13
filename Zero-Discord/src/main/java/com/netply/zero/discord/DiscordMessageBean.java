package com.netply.zero.discord;

import com.netply.botchan.web.model.Reply;
import com.netply.zero.discord.chat.DiscordChatManager;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.messaging.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DiscordMessageBean {
    private final String botChanURL;
    private MessageListener messageListener;
    private DiscordChatManager discordChatManager;


    @Autowired
    public DiscordMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, DiscordChatManager discordChatManager) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(botChanURL);
        this.discordChatManager = discordChatManager;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForDiscordReplies() {
        String url = String.format("/replies?id=%s", String.valueOf(SessionManager.getClientID()));
        messageListener.checkReplies(url, this::parseReply);
    }

    private void parseReply(Reply reply) {
        discordChatManager.sendMessage(reply.getTarget(), reply.getMessage());
    }
}
