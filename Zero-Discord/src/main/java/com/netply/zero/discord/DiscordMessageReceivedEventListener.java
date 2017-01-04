package com.netply.zero.discord;

import com.netply.botchan.web.model.Message;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.netply.zero.discord.status.StatusUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordMessageReceivedEventListener implements IListener<MessageReceivedEvent> {
    private String botChanURL;
    private TrackedUserManager trackedUserManager;


    public DiscordMessageReceivedEventListener(String botChanURL, TrackedUserManager trackedUserManager) {
        this.botChanURL = botChanURL;
        this.trackedUserManager = trackedUserManager;
    }

    public void handle(MessageReceivedEvent messageReceivedEvent) {
        String sender = messageReceivedEvent.getMessage().getAuthor().getID();
        String content = messageReceivedEvent.getMessage().getContent();
        Logger.getGlobal().log(Level.INFO, String.format("[Message] %s: %s\n", sender, content));
        if (!messageReceivedEvent.getMessage().getChannel().isPrivate()) {
            sender = messageReceivedEvent.getMessage().getChannel().getID();
        }
        trackedUserManager.addUser(sender);
        String url = String.format("/message?clientID=%s", String.valueOf(SessionManager.getClientID()));
        Service.create(botChanURL).put(url, new BasicSessionCredentials(), new Message(null, content, sender));
        StatusUtil.setLastMessageReceivedDate(new Date());
        StatusUtil.incrementReceivedMessagesCounter();
    }
}
