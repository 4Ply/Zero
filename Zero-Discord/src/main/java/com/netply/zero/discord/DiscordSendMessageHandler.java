package com.netply.zero.discord;

import com.netply.core.logging.Log;
import com.netply.zero.messaging.base.messaging.MessageHandler;
import sx.blah.discord.util.DiscordException;

import java.util.logging.Logger;

public class DiscordSendMessageHandler implements MessageHandler<SendDiscordMessage> {
    private static final Logger log = Log.getLogger();


    @Override
    public void parse(SendDiscordMessage message) {
        if (message.getUUID() == null || message.getMessage() == null) {
            return;
        }
        Database.getInstance().addToQueue(statement -> Database.logOutgoingDiscordMessage(statement, message.getUUID(), message.getMessage()), null);
        try {
            DiscordChatManager.getInstance().sendMessage(message.getUUID(), message.getMessage());
        } catch (DiscordException e) {
            log.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
