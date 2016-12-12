package com.netply.core.running;

import java.io.IOException;

@FunctionalInterface
public interface Parser {
    void invoke() throws InterruptedException, IOException;
}
