package com.netply.zero.messaging.base.messaging;

import com.netply.core.logging.Log;
import com.netply.core.running.ProcessRunner;
import com.netply.zero.messaging.base.poco.BaseMessage;
import com.netply.zero.messaging.base.poco.BaseReplyableMessage;
import com.netply.zero.messaging.base.poco.MessageType;

import java.util.HashMap;
import java.util.logging.Logger;

public class MessageParserManager {
    private static final Logger log = Log.getLogger();
    private static MessageParserManager instance;
    private HashMap<MessageType, MessageHandler<? extends BaseMessage>> messageParsers = new HashMap<>();
    private HashMap<MessageType, MessageHandler<? extends BaseReplyableMessage>> replyableMessageParsers = new HashMap<>();


    public MessageParserManager() {
//        replyableMessageParsers.put(MessageType.WHATSAPP_RECEIVED, new WhatsAppReceivedMessageHandler());
//        messageParsers.put(MessageType.WHATSAPP_SEND, new WhatsAppSendMessageHandler());
//        messageParsers.put(MessageType.DISCORD_SEND, new DiscordSendMessageHandler());
    }

    public static MessageParserManager getInstance() {
        if (instance == null) {
            instance = new MessageParserManager();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void parseInput() throws InterruptedException {
        while (ProcessRunner.run) {
            BaseMessage message = MessageQueue.getInstance().getNextQueueElement();
            MessageType type = message.getType();
            if (messageParsers.containsKey(type)) {
                MessageHandler messageHandler = messageParsers.get(type);
                messageHandler.parse(message);
            } else if (message instanceof ReplyableMessage && replyableMessageParsers.containsKey(type)) {
                MessageHandler<BaseReplyableMessage> messageHandler = (MessageHandler<BaseReplyableMessage>) replyableMessageParsers.get(type);
                messageHandler.parse((BaseReplyableMessage) message);
            } else {
                log.severe("No parser found for type: " + type);
            }
        }
    }
}
