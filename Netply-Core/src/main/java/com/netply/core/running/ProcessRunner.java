package com.netply.core.running;

import java.io.IOException;

public class ProcessRunner {
    public static boolean run = true;


    public static void startParserThread(Parser parser, long delay) {
        new Thread() {
            @Override
            public void run() {
                while (ProcessRunner.run) {
                    try {
                        parser.invoke();
                        Thread.sleep(delay);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

//    public static void startReadWhatsAppInput() throws IOException {
//        startParserThread(WhatsAppIOManager.getInstance()::postRunReadInput, 5000);
//    }
}
