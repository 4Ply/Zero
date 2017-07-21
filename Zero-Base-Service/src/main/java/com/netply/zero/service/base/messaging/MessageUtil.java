package com.netply.zero.service.base.messaging;

import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.botchan.web.model.ServerMessage;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;

public class MessageUtil {
    public static void reply(String baseURL, Message message, String replyMessage) {
        Service.create(baseURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getId(), replyMessage));
    }

    public static void sendMessage(String baseURL, Integer userID, String message) {
        Service.create(baseURL).put("/directMessage", new BasicSessionCredentials(), new ServerMessage(userID, message));
    }
}
