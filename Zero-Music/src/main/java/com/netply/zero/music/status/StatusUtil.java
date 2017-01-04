package com.netply.zero.music.status;

import java.util.Date;

public class StatusUtil {
    private static Date initDate;


    public static void setInitDate(Date initDate) {
        StatusUtil.initDate = initDate;
    }

    public static Date getInitDate() {
        return initDate;
    }
}
