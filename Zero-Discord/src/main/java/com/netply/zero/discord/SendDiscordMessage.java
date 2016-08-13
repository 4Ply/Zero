package com.netply.zero.discord;

import com.netply.zero.messaging.base.poco.BaseMessage;
import com.netply.zero.messaging.base.poco.MessageType;

public class SendDiscordMessage extends BaseMessage {
    private final String uuid;
    private final String message;


    public SendDiscordMessage(String uuid, String message) {
        super();
        this.uuid = uuid;
        this.message = message;
    }

    @Override
    public MessageType getType() {
        return MessageType.DISCORD_SEND;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
