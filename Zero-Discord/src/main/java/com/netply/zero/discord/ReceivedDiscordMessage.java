package com.netply.zero.discord;

import com.netply.core.logging.Log;
import com.netply.zero.messaging.base.messaging.ReplyableMessage;
import com.netply.zero.messaging.base.poco.BaseReplyableMessage;
import com.netply.zero.messaging.base.poco.MessageType;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

public class ReceivedDiscordMessage extends BaseReplyableMessage implements ReplyableMessage {
    private final IMessage message;
    private final IDiscordClient client;
    private final String content;


    public ReceivedDiscordMessage(IMessage message, IDiscordClient client, String content) {
        this.message = message;
        this.client = client;
        this.content = content;
    }

    @Override
    public String getMessage() {
        return content;
    }

    public IMessage getMessageObject() {
        return message;
    }

    public IDiscordClient getClient() {
        return client;
    }

    @Override
    public MessageType getType() {
        return MessageType.DISCORD_RECEIVED;
    }

    @Override
    public String getUUID() {
        return getClient().getOurUser().getID();
    }

    @Override
    public void reply(String message) {
        Log.getLogger().info(String.format("Discord reply to (%s): %s", getMessageObject().getAuthor().getName(), message));
        try {
            getMessageObject().reply(message);
        } catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
            e.printStackTrace();
        }
    }
}
