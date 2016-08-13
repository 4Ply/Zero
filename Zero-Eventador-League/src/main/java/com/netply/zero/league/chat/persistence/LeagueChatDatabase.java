package com.netply.zero.league.chat.persistence;

import com.netply.botchan.web.model.Message;

import java.util.List;

public interface LeagueChatDatabase {
    int addMessage(String targetUUID, String message);

    List<Message> getUnprocessedMessages();

    int processMessage(long id);
}
