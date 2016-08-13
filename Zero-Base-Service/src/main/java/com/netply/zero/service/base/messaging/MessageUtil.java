package com.netply.zero.service.base.messaging;

import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.Reply;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;

public class MessageUtil {
    public static void reply(String baseURL, Message message, String replyMessage) {
        Service.create(baseURL).put("/reply", new BasicSessionCredentials(), new Reply(message.getPlatform(), message.getSender(), replyMessage));
    }
}
