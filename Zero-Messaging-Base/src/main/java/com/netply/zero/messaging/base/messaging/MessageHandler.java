package com.netply.zero.messaging.base.messaging;

import com.netply.zero.messaging.base.poco.BaseMessage;

public interface MessageHandler<T extends BaseMessage> {
    void parse(T message);
}
