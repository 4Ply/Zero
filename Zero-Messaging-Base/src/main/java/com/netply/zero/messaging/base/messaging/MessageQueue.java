package com.netply.zero.messaging.base.messaging;


import com.netply.core.running.queue.QueueManger;
import com.netply.zero.messaging.base.poco.BaseMessage;

public class MessageQueue extends QueueManger<BaseMessage> {
    private static MessageQueue messageQueue;


    public static MessageQueue getInstance() {
        if (messageQueue == null) {
            messageQueue = new MessageQueue();
        }
        return messageQueue;
    }
}
