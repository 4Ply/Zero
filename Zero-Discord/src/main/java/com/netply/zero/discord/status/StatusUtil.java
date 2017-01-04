package com.netply.zero.discord.status;

import java.util.Date;

public class StatusUtil {
    private static Date initDate;
    private static Date lastMessageReceivedDate;
    private static long receivedMessages = 0;


    public static void setInitDate(Date initDate) {
        StatusUtil.initDate = initDate;
    }

    public static Date getInitDate() {
        return initDate;
    }

    public static void setLastMessageReceivedDate(Date lastMessageReceivedDate) {
        StatusUtil.lastMessageReceivedDate = lastMessageReceivedDate;
    }

    public static Date getLastMessageReceivedDate() {
        return lastMessageReceivedDate;
    }

    public static void incrementReceivedMessagesCounter() {
        StatusUtil.receivedMessages++;
    }

    public static long getReceivedMessages() {
        return receivedMessages;
    }
}
