package com.netply.zero.discord;

import com.netply.botchan.web.model.Message;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.netply.zero.discord.status.StatusUtil;
import com.netply.zero.service.base.Service;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
        String sender = messageReceivedEvent.getMessage().getAuthor().getStringID();
        String content = messageReceivedEvent.getMessage().getContent();
        Logger.getGlobal().log(Level.INFO, String.format("[Message] %s: %s\n", sender, content));
        if (!messageReceivedEvent.getMessage().getChannel().isPrivate()) {
            sender = messageReceivedEvent.getMessage().getChannel().getStringID();
        }

        boolean isDirectMessage = isDirectMessage(messageReceivedEvent);
        trackedUserManager.addUser(sender);
        Service.create(botChanURL).put("/message", new Message(content, sender, isDirectMessage));
        StatusUtil.setLastMessageReceivedDate(new Date());
        StatusUtil.incrementReceivedMessagesCounter();
    }

    private boolean isDirectMessage(MessageReceivedEvent messageReceivedEvent) {
        return messageReceivedEvent.getMessage().getChannel().isPrivate() ||
                messageReceivedEvent.getMessage().getMentions().stream().filter(IUser::isBot).anyMatch(iUser -> iUser.getStringID().equals(botChanUserID.get()));
    }
}
