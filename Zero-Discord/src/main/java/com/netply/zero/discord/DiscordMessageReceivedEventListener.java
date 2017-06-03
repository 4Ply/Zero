package com.netply.zero.discord;

import com.netply.botchan.web.model.Message;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.netply.zero.discord.status.StatusUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordMessageReceivedEventListener implements IListener<MessageReceivedEvent> {
    private String botChanURL;
    private TrackedUserManager trackedUserManager;
    private Supplier<String> botChanUserID;


    public DiscordMessageReceivedEventListener(String botChanURL, TrackedUserManager trackedUserManager, Supplier<String> botChanUserID) {
        this.botChanURL = botChanURL;
        this.trackedUserManager = trackedUserManager;
        this.botChanUserID = botChanUserID;
    }

    public void handle(MessageReceivedEvent messageReceivedEvent) {
        String sender = messageReceivedEvent.getMessage().getAuthor().getID();
        String content = messageReceivedEvent.getMessage().getContent();
        Logger.getGlobal().log(Level.INFO, String.format("[Message] %s: %s\n", sender, content));
        if (!messageReceivedEvent.getMessage().getChannel().isPrivate()) {
            sender = messageReceivedEvent.getMessage().getChannel().getID();
        }

        boolean isDirectMessage = isDirectMessage(messageReceivedEvent);
        trackedUserManager.addUser(sender);
        String url = String.format("/message?clientID=%s", String.valueOf(SessionManager.getClientID()));
        Service.create(botChanURL).put(url, new BasicSessionCredentials(), new Message(null, content, sender, isDirectMessage));
        StatusUtil.setLastMessageReceivedDate(new Date());
        StatusUtil.incrementReceivedMessagesCounter();
    }

    private boolean isDirectMessage(MessageReceivedEvent messageReceivedEvent) {
        return messageReceivedEvent.getMessage().getChannel().isPrivate() ||
                messageReceivedEvent.getMessage().getMentions().stream().filter(IUser::isBot).anyMatch(iUser -> iUser.getID().equals(botChanUserID.get()));
    }
}
