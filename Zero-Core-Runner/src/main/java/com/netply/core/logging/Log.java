package com.netply.core.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    static {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
//        FileHandler fh;

        try {
//            String fileName = Constants.HOME_RUN_DIR + "manga_hunter.log";
//            fh = new FileHandler(fileName, 100000, 1);
//            logger.addHandler(fh);
            logger.setLevel(Level.INFO);

//            SimpleFormatter formatter = new SimpleFormatter();
//            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
}
