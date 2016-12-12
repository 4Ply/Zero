package com.netply.core.running.queue;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class QueueManger<T> {
    protected final BlockingDeque<T> queue = new LinkedBlockingDeque<>();


    public void add(T message) {
        queue.add(message);
    }

    public T getNextQueueElement() throws InterruptedException {
        return queue.take();
    }
}
