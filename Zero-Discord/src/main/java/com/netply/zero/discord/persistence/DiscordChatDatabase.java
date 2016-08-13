package com.netply.zero.discord.persistence;

import com.netply.botchan.web.model.Message;

import java.util.List;

public interface DiscordChatDatabase {
    int addMessage(String targetUUID, String message);

    List<Message> getUnprocessedMessages();

    int processMessage(long id);
}
