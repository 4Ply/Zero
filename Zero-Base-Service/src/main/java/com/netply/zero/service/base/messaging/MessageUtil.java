package com.netply.zero.service.base.messaging;

import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.botchan.web.model.ServerMessage;
import com.netply.zero.service.base.Service;

public class MessageUtil {
    public static void reply(String baseURL, Message message, String replyMessage) {
        Service.create(baseURL).put("/reply", new Reply(message.getId(), replyMessage));
    }

    public static void sendMessage(String baseURL, Integer userID, String message) {
        Service.create(baseURL).put("/directMessage", new ServerMessage(userID, message));
    }
}
